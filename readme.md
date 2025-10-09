# Spring Batch - 레시피 데이터 처리 프로젝트

Spring Batch를 활용한 레시피 데이터 정제 및 더미 데이터 생성 프로젝트

## 주요 기능

### 1. 레시피 데이터 처리 (RecipeJob)
- MongoDB에서 원본 레시피 데이터 읽기
- Gemini API (Gemma3 모델)를 통한 데이터 정제
- 처리된 데이터를 MongoDB에 저장
- 에러 핸들링 및 Skip Listener를 통한 실패 처리

### 2. 더미 데이터 생성 (DummyDataJob)
- Member, Post, Comment, Category 등 더미 데이터 생성
- MySQL 데이터베이스에 저장
- Gemini API를 활용한 자동 콘텐츠 생성

## 기술 스택

- **Framework**: Spring Boot, Spring Batch
- **Database**:
  - MongoDB (레시피 데이터)
  - MySQL (더미 데이터, Batch 메타데이터)
- **AI**: Google Gemini API (Gemma3 모델)

## Gemini API 제약사항

| 모델 | 분당 요청 수 (RPM) | 분당 토큰 수 (TPM) | 일일 요청 수 (RPD) |
|------|-------------------|-------------------|-------------------|
| Gemma3 | 30 | 15,000 | 14,400 |

- 레시피 1개당 약 2,000 토큰 사용 (12B 모델 기준)
- 30초마다 배치 처리 권장

## 실행 방법

### 프로필 설정
- `mongo`: MongoDB 연결
- `gemini`: Gemini API 설정
- `prompt`: 프롬프트 설정


## 에러 처리

- `BatchError` 테이블에 실패한 작업 기록
- Skip Listener를 통한 실패 항목 추적
- 재처리 가능한 구조

## 로그

- 로그 파일 위치: `/app/logs/spring-batch.log`
- Docekr Volume (바인드 마운트) 설정

## 참고사항

- 처리 대상 데이터: 약 8,000개
- ID 기반 인덱스를 활용한 효율적인 데이터 읽기
- Docker 환경 + Compose 지원