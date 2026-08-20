# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
# 의존성 캐시 레이어 (선택) — 소스 복사 전에 wrapper 만으로 준비
RUN chmod +x gradlew
COPY src ./src
RUN ./gradlew clean bootJar --no-daemon

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
