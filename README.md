# Digital Procurement Platform for Agriculture & Food Production

Production-ready, cloud-native microservices architecture built with **Spring Boot 3.3**, **Java 17**, **Gradle**, and event-driven patterns (CQRS, Saga, Outbox). The platform delivers fair pricing, end-to-end traceability, and trusted payments in agricultural supply chains.

## Modules
- shared-kernel: common domain primitives and event contracts
- gateway-service: API gateway with rate limiting and routing
- services:
  - procurement-service
  - product-service
  - payment-service
  - logistics-service
  - certification-service
  - notification-service
  - analytics-service

## Local Development
```bash
# Start infrastructure (PostgreSQL, MongoDB, Redis, Kafka, Observability)
docker compose up -d

# Build all services
./gradlew clean build

# Run a service
./gradlew :services:procurement-service:bootRun
```

## Observability
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- Loki: http://localhost:3100

## Architecture Docs
See `docs/architecture.md` for full technical architecture details.