# Spring Batch Project

- Spring Batch 프레임워크 기반 데이터 정제, 삽입 프로젝트

## 주요 아키텍처 요약

![Spring Batch 아키텍처](https://res.cloudinary.com/dtrxriyea/image/upload/v1774090549/etc/c7nij7l3wunot5ftzhqa.avif)

## Job 별 주요 구현 내용

### RecipeJob

#### 구현 목표

- 불완전한 원본 레시피 데이터를 Gemini API (Gemma 3 모델)을 활용해 구조화된 데이터로 정제 및 보완
- 크롤링 데이터 누락된 필드 복원 및 데이터 품질 향상 목적

#### 구현 내용 

##### Reader : `MongoPagingItemReader`

- **구현**:  흔히 사용하는 Offset 방식(`skip`, `limit`), 커서 방식 대신 **No-Offset(Keyset)** 방식을 구현
  - `Gemma3`모델(무료 모델) 특성 상 요청 term 을 가져야 하므로
  - **ExecutionContext** 이용 구현
- **performance**: 마지막 처리된 `_id`를 기준으로 인덱스 스캔(`gt`)을 수행하여 데이터 양이 늘어나도 조회 속도가 일정하게 유지
- **Chunk 전략**: DB 조회는 효율을 위해 100개(`PAGE_SIZE`)씩 수행하지만, Processor로는 2개(`PAIR_SIZE`)씩 전달하여 AI 모델의 Context Window 효율 최적화

##### Processor: `GeminiRecipeProcessor`

- 2개의 레시피를 하나의 프롬프트로 병합 처리하여 API 호출 비용 최적화
- `Gemma3` 모델의 Rate Limit(무료 제한)을 준수하기 위해 처리 로직 내에 `Thread.sleep`을 적용, 안정적인 파이프라인을 구축

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

- `DummyDataProcessor`가 의존하는 메타데이터(Member, Category, Tag) 자동 생성 및 DB 상태 동기화

##### Reader: `MongoCursorItemReader`

- DB 커서를 유지하는 스트리밍 방식을 채택하여 대용량 처리 시 OOM(Out Of Memory) 방지

##### Processor: `DummyDataProcessor`

- **캐싱 처리**: `ItemStream.open()` 시점에 자주 사용되는 메타 데이터(Category, Tag, Member)를 메모리에 캐싱 반복적 I/O 제거
- **비지니스 로직**: 1개의 Recipe에 포함된 **각 조리 순서(Step)마다 100개씩** Post를 생성하여 대량의 데이터로 증폭 반환 (1 Input -> N * 100 Outputs)

##### Writer: `DummyDataWriter`

- **Batch Save**: 병합된 리스트를 JPA `saveAll()`로 일괄 저장하여 DB 커넥션 오버헤드를 감소
- Post 저장 후 생성된 ID를 활용, 연관 테이블(PostTag) 데이터까지 동일 트랜잭션 내에서 처리

---
## 예외 처리 및 스킵 전략

### Skip 전략
- 일시적인 API 호출 오류(Gemma 3)나 특정 데이터 포맷 결함 발생 시, 전체 작업 중단 없이 해당 아이템만 Skip 처리
- 안정성 확보: 대량 데이터 처리 중 발생하는 예외 상황에 유연하게 대응하여 파이프라인의 가용성 극대화
### 비즈니스 에러 로깅 (BatchError)
- 관심사의 분리: Spring Batch 프레임워크의 실행 이력(Meta-data)과 실제 비즈니스 데이터의 결함(Domain Error)을 별도 테이블로 분리 관리
- 데이터 정체성 유지: 개별 에러 로그 테이블 운용을 통해 비즈니스 도메인 데이터의 무결성 및 추적성 확보
### 모니터링 및 재시도 전략
- 장애 추적성: Skip된 아이템의 상세 사유를 `BatchError` 엔티티에 기록하여 사후 데이터 보정 및 원인 파악 지원
- (여기에 추가하고 싶은 세부 전략이 있다면 직접 채워보세요!)


## 기술 스택

Batch Framework: Spring Batch 5
Language: Java 17
Database: MongoDB, MySQL
AI: Google Gemini (Gemma 3)
Infra: Docker, Docker Compose
