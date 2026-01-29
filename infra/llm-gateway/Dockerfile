# Stage 1: Build the Application
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY . .
# Skip tests for speed in this example, but usually run them
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Run the Application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose Web Port and gRPC Port
EXPOSE 8080 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
