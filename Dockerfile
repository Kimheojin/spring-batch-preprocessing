# 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x

# 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN mkdir -p /app/logs
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]