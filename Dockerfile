# 멀티 스테이지로 설정
# 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . /app
RUN chmod +x ./gradlew
RUN ./gradlew build -x

# 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN mkdir -p /app/logs # 이미 존재해도 에러 발생 X
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]