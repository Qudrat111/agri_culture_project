## Build once, deploy any module by passing --build-arg SERVICE=<gradle-module>
## Example: docker build -t agri/order:dev --build-arg SERVICE=procurement-order-service .

FROM gradle:8.8-jdk21 AS builder

ARG SERVICE
WORKDIR /workspace

# Copy everything (multi-module build)
COPY . .

# Build the requested Spring Boot module
RUN ./gradlew :${SERVICE}:bootJar --no-daemon

FROM eclipse-temurin:21-jre

ARG SERVICE
WORKDIR /app

COPY --from=builder /workspace/${SERVICE}/build/libs/*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
