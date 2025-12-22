# 멀티 스테이지 빌드
# 1. 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle 래퍼 및 설정 파일만 먼저 복사 (캐시 활용을 위해)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 실행 권한 부여
RUN chmod +x ./gradlew

RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 실제 빌드 수행 (테스트 제외)
RUN ./gradlew build -x test --no-daemon

# 2. 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN mkdir -p /app/logs
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
