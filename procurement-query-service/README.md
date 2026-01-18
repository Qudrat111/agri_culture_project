# Procurement Query Service

## Overview

The **Procurement Query Service** is the Read Side of the CQRS (Command Query Responsibility Segregation) architecture for the Digital Procurement Platform. It maintains a denormalized read model optimized for fast query operations using MongoDB.

## Architecture

- **Event-Driven**: Listens to domain events from Kafka topics
- **Eventually Consistent**: Updates read model asynchronously via event projectors
- **Denormalized Data**: Stores complete order views with embedded data for optimal read performance
- **Rate Limited**: 200 requests per minute per client (higher than command side)

## Technology Stack

- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 3.3.7**: Application framework
- **Spring Data MongoDB**: NoSQL database access
- **Apache Kafka**: Event streaming platform
- **Bucket4j**: Rate limiting
- **OpenTelemetry**: Distributed tracing
- **Lombok**: Boilerplate code reduction

## Project Structure

```
procurement-query-service/
├── src/main/java/com/agri/query/
│   ├── QueryServiceApplication.java           # Main application entry point
│   ├── model/
│   │   ├── OrderView.java                     # MongoDB document for order views
│   │   └── OrderItemView.java                 # Embedded document for order items
│   ├── repository/
│   │   └── OrderViewRepository.java           # Spring Data MongoDB repository
│   ├── projector/
│   │   └── OrderProjector.java                # Kafka event listener & projector
│   ├── api/
│   │   ├── OrderQueryController.java          # REST API endpoints
│   │   └── OrderViewResponse.java             # Response DTO
│   └── infrastructure/
│       ├── config/
│       │   └── KafkaConsumerConfig.java       # Kafka consumer configuration
│       └── ratelimit/
│           └── RateLimiterFilter.java         # Rate limiting filter
└── src/main/resources/
    └── application.yml                         # Application configuration
```

## Data Model

### OrderView Document

```java
@Document(collection = "order_views")
{
    "id": "order-uuid",
    "buyerId": "buyer-uuid",
    "buyerName": "Buyer-{buyerId}",           // Denormalized
    "supplierId": "supplier-uuid",
    "supplierName": "Supplier-{supplierId}",  // Denormalized
    "items": [
        {
            "productId": "product-uuid",
            "productName": "Product Name",
            "quantity": 100.00,
            "price": 50.00,
            "unit": "kg"
        }
    ],
    "status": "PENDING|CONFIRMED|CANCELLED|COMPLETED",
    "totalAmount": 5000.00,
    "createdAt": "2024-01-18T10:30:00Z",
    "updatedAt": "2024-01-18T10:30:00Z"
}
```

**Indexes:**
- `buyerId` - For buyer queries
- `supplierId` - For supplier queries
- `status` - For status filtering
- `createdAt` - For sorting

## Event Processing

The service listens to `procurement.procurementorder.events` Kafka topic and handles:

1. **OrderCreatedEvent**: Creates new OrderView with denormalized data
2. **OrderConfirmedEvent**: Updates status to CONFIRMED
3. **OrderCancelledEvent**: Updates status to CANCELLED
4. **OrderCompletedEvent**: Updates status to COMPLETED

### Retry Policy
- Max attempts: 3
- Initial delay: 1000ms
- Multiplier: 2 (exponential backoff)

### MDC Logging
All event processing includes `orderId` in the logging context for traceability.

## REST API Endpoints

### Get Order by ID
```
GET /api/v1/orders/{orderId}
```

**Response:**
```json
{
    "id": "order-uuid",
    "buyerId": "buyer-uuid",
    "buyerName": "Buyer-buyer-uuid",
    "supplierId": "supplier-uuid",
    "supplierName": "Supplier-supplier-uuid",
    "items": [...],
    "status": "PENDING",
    "totalAmount": 5000.00,
    "createdAt": "2024-01-18T10:30:00Z",
    "updatedAt": "2024-01-18T10:30:00Z"
}
```

### Get Orders by Buyer
```
GET /api/v1/orders/buyer/{buyerId}?page=0&size=20
```

### Get Orders by Supplier
```
GET /api/v1/orders/supplier/{supplierId}?page=0&size=20
```

### Get All Orders (with optional status filter)
```
GET /api/v1/orders?status=PENDING&page=0&size=20
```

**Pagination:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- Results sorted by `createdAt` descending

## Rate Limiting

- **Limit**: 200 requests per minute per client
- **Client Identification**: `X-Client-Id` header or IP address
- **Response**: 429 Too Many Requests when limit exceeded
- **Algorithm**: Token bucket with Bucket4j

## Configuration

### MongoDB
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/agri_procurement_read
```

### Kafka Consumer
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: query-projector-group
      auto-offset-reset: earliest
```

### Observability
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
```

## Running the Service

### Prerequisites
- Java 21
- MongoDB running on localhost:27017
- Kafka running on localhost:9092

### Build
```bash
./gradlew :procurement-query-service:build
```

### Run
```bash
./gradlew :procurement-query-service:bootRun
```

The service will start on port **8083**.

### Health Check
```bash
curl http://localhost:8083/actuator/health
```

## Monitoring

### Metrics
- Available at: `http://localhost:8083/actuator/metrics`
- Prometheus endpoint: `http://localhost:8083/actuator/prometheus`

### Tracing
- OTLP traces sent to `http://localhost:4318/v1/traces`
- Compatible with Jaeger, Zipkin, etc.

## Development Notes

### Denormalization Strategy
Currently using placeholder names for buyers and suppliers (`Buyer-{id}`, `Supplier-{id}`). In production, the projector should:
1. Call User Service to fetch actual names
2. Update OrderView with real names
3. Handle name changes via UserUpdated events

### Event Sourcing
The read model is **eventually consistent**. There may be a brief delay between:
1. Command execution (write side)
2. Event publishing
3. Event consumption
4. Read model update

Typical latency: < 100ms

### MongoDB Schema
MongoDB is schemaless, so no migrations are needed. Indexes are created automatically via Spring Data annotations.

## Testing

### Example: Query Order
```bash
curl -H "X-Client-Id: test-client" \
     http://localhost:8083/api/v1/orders/{orderId}
```

### Example: Query by Buyer
```bash
curl -H "X-Client-Id: test-client" \
     "http://localhost:8083/api/v1/orders/buyer/{buyerId}?page=0&size=10"
```

### Example: Trigger Rate Limit
```bash
for i in {1..250}; do
    curl -H "X-Client-Id: test-client" \
         http://localhost:8083/api/v1/orders?status=PENDING
done
```

After 200 requests, you'll receive:
```json
{
    "error": "Too Many Requests",
    "message": "Rate limit exceeded. Maximum 200 requests per minute."
}
```

## Future Enhancements

1. **Caching**: Add Redis for frequently accessed orders
2. **Full-Text Search**: Integrate Elasticsearch for advanced queries
3. **Real Names**: Integrate with User Service for actual buyer/supplier names
4. **Materialized Views**: Add aggregate views (e.g., daily totals)
5. **Event Replay**: Support rebuilding read model from event store
6. **Pagination Metadata**: Return total count, page info in responses
7. **GraphQL**: Add GraphQL API for flexible querying

## Dependencies

See `build.gradle` for complete dependency list. Key dependencies:
- Spring Boot Starter Web
- Spring Boot Starter Data MongoDB
- Spring Kafka
- Spring Retry
- Bucket4j
- OpenTelemetry
- Lombok
