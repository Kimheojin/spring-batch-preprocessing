### Spring Batch 실행 계획

개인 온프레미스 서버에 하루 정도 돌리기

## reader 부분

cursor 기반 -> paging 기반 -> 생각보다 오래걸려 연결 문제 생길 수 있음

그냥 id 기반으로 인덱스 태우는 게 맞을듯

### Gemma3 스펙

| 모델 | 분당 요청 수 (RPM) | 분당 토큰 수 (TPM) | 일일 요청 수 (RPD) |
|------|-------------------|-------------------|-------------------|
| Gemma 3 및 3n | 30 | 15,000 | 14,400 |

### 데이터 갯수 

약 8000개 


#### 예상 제약 사항

- mongo altas 요청수에 따른 제약 예상
  - 확인 해 보니 Gemma3 모델 병목으로 인해 제약 없을 듯 
- 레시피 한개 당 12B 모델은 약 2000개 토큰, 27B의 경우 약 4000개 토큰 사용
  - 12B 정도로 하는 게 좋을 듯
- 30초 마다 요청 보내게 하면 될듯(2개씩 Tasklet 에 묶어서)

### Gemini

다음 2개 혹은 1개의 레시피 데이터에서 각각의 ingredientList를 cookingOrderList의 조리과정을 분석해서 완성해주세요.

조건:
- 기존 ingredientList는 참고만 하고, cookingOrderList에서 언급된 모든 재료를 추출
- 재료명만 추출 (수량, 단위 제외)
- 예: "쌀 2컵" → "쌀", "무청 200g" → "무청", "쌀뜨물" → "쌀뜨물"
- 조리 도구나 방법은 제외 (냄비, 강판, 체 등)

JSON 배열 형식으로 응답해주세요:
[
{
"recipeName": "레시피명1",
"sourceUrl": "출처URL1",
"siteIndex": "사이트인덱스1",
"ingredientList": ["완성된 재료 리스트1"],
"cookingOrderList": [원본 그대로 유지1]
},
{
"recipeName": "레시피명2",
"sourceUrl": "출처URL2",
"siteIndex": "사이트인덱스2",
"ingredientList": ["완성된 재료 리스트2"],
"cookingOrderList": [원본 그대로 유지2]
...
## 배포저 할 꺼
- 포트포워딩 연결 후 메타 스키마 생성되는 지 확인하기
- mysql docker container 로 확인하깅
- 이거 배포 방식 생각하기
  - 아마 git runner 기존 방식으로 할듯
- 그 reader 부분 강제로 제한 걸었던 거 풀기
- 에러 관련 생각해서 처리 준비 해야 할 수도
- 에러 발생 시 어떻게 처리할 지 생각하기
  - 로그만으로 처리하기엔 좀 그래보임
## 재처리 관련
- 로그로 관리할 지
- 커스텀 테이블 만들어서 할 지
- ExcutionContext 에 넣어서 할지

## 런타임 단계 초기화 설정 관련 정리


- application.yml
  - job launcher 관련 설정
  - 로그 파일 위치 지정
    -  이거 docker container 띄워진 후 동작하는 거라 상관 없다고 함
  - 프로파일 active 관련 설정
    - 현재 mongo, gemini, prompt 지정
  - 졸리다

### docker 파일 관련

- 포트 명시적으로만 열변 될듯



      