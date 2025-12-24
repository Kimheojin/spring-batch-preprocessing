# syntax=docker/dockerfile:1.6
# 멀티 스테이지 빌드
# 1. 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle 설정/래퍼 먼저 복사 (의존성 캐시 레이어 분리)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew

# Gradle 캐시(Wrapper dists + dependency cache) 영속화
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

# 소스 코드 복사 (자주 바뀌는 부분)
COPY src src

# 빌드 수행 (테스트 제외) - 동일 캐시 사용
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew build -x test --no-daemon


# 2. 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN mkdir -p /app/logs
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
