# Spring Batch Project

- Spring Batch 프레임워크 기반 데이터 정제, 삽입 프로젝트

## 주요 아키텍처 요약

![Spring Batch Job 주요 아키텍처](https://res.cloudinary.com/dtrxriyea/image/upload/v1774090549/etc/c7nij7l3wunot5ftzhqa.avif)

## Job 별 구현 내용

### RecipeJob

#### 구현 목표

- 불완전한 원본 레시피 데이터를 Gemini API (Gemma 3 모델)을 활용해 구조화된 데이터로 정제 및 보완
- 크롤링 데이터 누락된 필드 복원 및 데이터 품질 향상 목적

#### 구현 내용 

##### Reader : `MongoPagingItemReader`

- **구현**: No-Offset(Keyset) 기반 직접적인 상태 관리 (`ExecutionContext` 활용)
  - `Gemma3` 모델 특성 상 요청 간 term 유지를 위해 서버가 마지막 처리 위치를 직접 관리
  
```java
private static final String LAST_PROCESSED_ID = "last.processed.id";

@Override
public void open(ExecutionContext executionContext) {
    this.lastProcessedId = executionContext.getString(LAST_PROCESSED_ID);
}

@Override
public void update(ExecutionContext executionContext) {
    executionContext.putString(LAST_PROCESSED_ID, lastProcessedId);
}
```
  
- **Performance**: `_id` 기준 인덱스 스캔(`gt`)으로 데이터 양과 무관하게 조회 속도 유지

```java
query.addCriteria(Criteria.where("_id").gt(new ObjectId(lastProcessedId)));
query.limit(PAGE_SIZE); // 100개씩 벌크 로드
```

- **Chunk 전략**: DB 조회(100) vs Processor 전달(2) 분리하여 AI 모델의 Context Window 효율 최적화
  - Reader에서 2개씩 묶어 반환하며, Chunk Size를 10으로 설정하여 한 트랜잭션당 총 20개의 레시피를 일괄 처리 및 적재

```java
// PAIR_SIZE(2)만큼 묶어서 Processor로 전달
for (int i = 0; i < PAIR_SIZE && currentIndex < currentBatch.size(); i++) {
    batch.add(currentBatch.get(currentIndex++));
}
```

##### Processor: `GeminiRecipeProcessor`

- 2개의 레시피를 하나의 프롬프트로 병합 처리하여 API 호출 비용 최적화
- `Gemma3` 모델의 Rate Limit(무료 제한)을 준수하기 위해 처리 로직 내에 `Thread.sleep(30000)`을 적용, 안정적인 파이프라인을 구축

##### Writer: `MongoRecipeWriter`

- Stream API를 활용한 중첩 리스트 평탄화(FlatMap) 및 필수 필드 유효성 검사
- 검증된 레시피 데이터만 MongoDB에 최종 적재

```java
  // 실제 구현 로직 요약
  chunk.getItems().stream()
       .flatMap(List::stream)      // 중첩 리스트 평탄화
       .filter(this::isValidRecipe) 
       .collect(Collectors.toList());
```

### DummyDataJob

#### 구현 목표

- 부하 테스트를 위한 대규모 더미 데이터(Post, Member, Comment) 생성 및 적재
- 단일 레시피 기반의 대량 데이터 증폭을 통한 더미 데이터 삽입

#### 구현 내용

##### `InitStep` (Tasklet)

- 본 작업 실행 전 Member, Category, Tag 등 기초 메타데이터를 우선 생성 및 DB 동기화

```java
// 관리자 계정, 카테고리(100개), 태그(50개) 자동 생성 및 동기화
@Override
public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    if(memberRepository.findByEmail("hurjin1109@naver.com").isEmpty()){
        // 관리자 계정 및 ADMIN Role 생성 로직
        memberRepository.save(Member.builder().email("hurjin1109@naver.com").role(adminRole).build());
    }
    initCategory(); // 목표치(100개)까지 카테고리 자동 생성
    initTag();      // 목표치(50개)까지 태그 자동 생성
    return RepeatStatus.FINISHED;
}
```

##### Reader: `MongoPagingItemReader`

- 페이징 기반 조회를 통해 대용량 데이터 처리 시 메모리 점유율 최소화 (OOM 방지) 및 안정적인 데이터 로드

```java
// MongoPagingItemReaderBuilder를 통한 페이징 조회 설정
return new MongoPagingItemReaderBuilder<RawRecipe>()
        .name("dummyDataReader").template(mongoTemplate)
        .targetType(RawRecipe.class).jsonQuery("{}")
        .sorts(Collections.singletonMap("_id", Sort.Direction.ASC))
        .pageSize(10).build();
```

##### Processor: `DummyDataProcessor`

- `ItemStream.open()` 단계에서 자주 참조되는 도메인 엔티티(회원, 카테고리 등)를 메모리에 캐싱하여 DB 부하 최소화

```java
@Override
public void open(ExecutionContext executionContext) {
    // Step 시작 시 1회만 로딩하여 메모리에 캐싱 (DB 부하 최소화)
    categories = categoryRepository.findAll();
    allTagIds = tagRepository.findAll().stream()
            .map(Tag::getId).collect(Collectors.toList());
    defaultMember = memberRepository.findByEmail("hurjin1109@naver.com").orElseThrow();
}
```

##### Writer: `DummyDataWriter`

- `saveAll()`을 활용하여 다수의 엔티티를 일괄 저장하여 쓰기 성능 확보
- `Post` 저장 후 생성된 ID를 직접 매핑하여 `PostTag`를 벌크 저장
  - 연관관계 매핑 시 발생하는 N+1 Select 및 개별 Insert 오버헤드 방지

```java
@Override
public void write(Chunk<? extends List<Post>> chunk) throws Exception {
    List<Post> posts = chunk.getItems().stream().flatMap(List::stream).toList();
    List<Post> savedPosts = postRepository.saveAll(posts);

    // 저장된 Post ID를 활용한 PostTag 벌크 생성 및 저장
    List<PostTag> tags = savedPosts.stream()
            .flatMap(p -> p.getTagIds().stream().map(tId -> PostTag.builder().postId(p.getId()).tagId(tId).build()))
            .toList();

    if (!tags.isEmpty()) postTagRepository.saveAll(tags);
}
```


## 예외 처리 및 장애 대응 전략

### Skip 전략

- API 호출(Gemma 3) 오류나 데이터 포맷 결함 발생 시 전체 중단 없이 해당 아이템만 Skip 처리
- `SkipListener`를 통해 실패 지점(Reader, Processor, Writer)별 예외 상황을 독립적으로 제어

```java
@Override
public void onSkipInProcess(List<RawRecipe> items, Throwable t) {
    for (RawRecipe item : items) {
        // 실패한 아이템의 메타데이터(URL, Index)를 추출하여 에러 로그 기록
        BatchError error = BatchError.builder()
                .sourceUrl(item.getSourceUrl())
                .siteIndex(item.getSiteIndex())
                .build();
        batchErrorRepository.save(error);
    }
}
```

### 비즈니스 에러 로깅 (`BatchError`)

- Spring Batch 메타데이터와 도메인 에러 데이터를 별도 테이블로 분리 관리
- Skip된 아이템의 상세 정보를 `BatchError` 엔티티에 기록하여 사후 데이터 보정 및 원인 파악 지원

```java
@Entity
public class BatchError {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceUrl; // 원본 데이터 위치
    private String siteIndex; // 정제 실패 지점 식별자
}
```

## 기술 스택

- **Framework**: Spring Boot 3.5.4, Spring Batch 5.x
- **Language**: Java 17
- **Database**: MongoDB (데이터 적재/정제), MySQL (도메인/메타데이터), H2 (테스트 환경)
- **AI/LLM**: Gemini API (Gemma 3)
- **Infra/DevOps**: Docker, Docker Compose, Gradle 8.x
- **Core Library**: Spring Data JPA, Spring Data MongoDB, Lombok, Apache HttpClient5
