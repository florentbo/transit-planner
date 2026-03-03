FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY api-spec/ api-spec/
COPY backend/ backend/
WORKDIR /app/backend
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/backend/build/libs/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
