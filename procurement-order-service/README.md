# Procurement Order Service

Command Side of the CQRS architecture for the Digital Procurement Platform.

## Overview

This service handles all write operations (commands) for procurement orders, following Domain-Driven Design (DDD) principles and the Transactional Outbox pattern for reliable event publishing.

## Architecture

### Domain Layer
- **Aggregate Root**: `ProcurementOrder` - Encapsulates order state and business rules
- **Value Objects**: 
  - `BuyerId` - Type-safe buyer identifier
  - `SupplierId` - Type-safe supplier identifier
  - `OrderItem` - Embedded order line item with validation
- **Domain Events**: 
  - `OrderCreatedEvent`
  - `OrderConfirmedEvent`
  - `OrderCancelledEvent`
  - `OrderCompletedEvent`
- **State Machine**: PENDING → CONFIRMED → COMPLETED, or → CANCELLED

### Application Layer
- `OrderCommandService` - Handles command execution with transactional guarantees
- `OrderNotFoundException` - Domain-specific exception

### Infrastructure Layer
- **Repositories**: JPA repositories for persistence
- **OutboxPublisher**: Scheduled job publishing events to Kafka
- **RateLimiterFilter**: Bucket4j-based rate limiting (100 req/min per client)

### API Layer
- REST endpoints for order commands
- Request/response DTOs with validation

## Features

### Idempotency
- Uses `X-Idempotency-Key` header to prevent duplicate order creation
- Keys expire after 24 hours
- Cached responses returned for duplicate requests

### Rate Limiting
- Per-client rate limiting using `X-Client-Id` header
- 100 requests per minute per client (configurable)
- Returns HTTP 429 when limit exceeded

### Transactional Outbox Pattern
- Domain events saved in same transaction as aggregate
- Separate scheduled publisher polls and sends events to Kafka
- Events marked as processed only after Kafka confirms receipt
- Prevents message loss and ensures exactly-once semantics

### Observability
- OpenTelemetry tracing with OTLP exporter
- MDC logging with traceId and orderId
- Prometheus metrics via Actuator
- Health checks and readiness probes

## API Endpoints

### Create Order
```http
POST /api/v1/orders
Headers:
  X-Idempotency-Key: <unique-key>
  X-Client-Id: <client-id>
  Content-Type: application/json

Request Body:
{
  "buyerId": "buyer-uuid",
  "supplierId": "supplier-uuid",
  "items": [
    {
      "productId": "product-uuid",
      "productName": "Product Name",
      "quantity": 10.00,
      "price": 99.99,
      "unit": "kg"
    }
  ]
}

Response: 201 Created
{
  "id": "order-uuid",
  "buyerId": "buyer-uuid",
  "supplierId": "supplier-uuid",
  "items": [...],
  "status": "PENDING",
  "totalAmount": 999.90,
  "createdAt": "2024-01-18T10:00:00Z",
  "updatedAt": "2024-01-18T10:00:00Z"
}
```

### Confirm Order
```http
POST /api/v1/orders/{orderId}/confirm

Response: 200 OK
```

### Cancel Order
```http
POST /api/v1/orders/{orderId}/cancel
Content-Type: application/json

Request Body:
{
  "reason": "Out of stock"
}

Response: 200 OK
```

## Configuration

### Environment Variables
- `DB_USERNAME` - PostgreSQL username (default: postgres)
- `DB_PASSWORD` - PostgreSQL password (default: postgres)

### Application Properties
- `procurement.order.outbox.publisher.fixed-delay` - Outbox polling interval (ms)
- `procurement.order.outbox.publisher.batch-size` - Max events per batch
- `procurement.order.ratelimit.tokens-per-minute` - Rate limit per client

## Database Schema

### Tables
- `procurement_orders` - Order aggregates
- `order_items` - Order line items (embedded collection)
- `outbox_events` - Transactional outbox for events
- `idempotency_keys` - Idempotency tracking

### Indexes
- Buyer/Supplier/Status indexes on orders
- Partial index on unprocessed outbox events
- Product index on order items

## Running the Service

### Prerequisites
- Java 21
- PostgreSQL 14+
- Kafka 3.x

### Build
```bash
./gradlew :procurement-order-service:build
```

### Run
```bash
./gradlew :procurement-order-service:bootRun
```

### With Dev Profile (SQL logging)
```bash
./gradlew :procurement-order-service:bootRun --args='--spring.profiles.active=dev'
```

### Docker Compose
```bash
docker-compose up -d postgres kafka
./gradlew :procurement-order-service:bootRun
```

## Testing

### Manual Testing
```bash
# Create order
curl -X POST http://localhost:8081/api/v1/orders \
  -H "X-Idempotency-Key: $(uuidgen)" \
  -H "X-Client-Id: test-client" \
  -H "Content-Type: application/json" \
  -d '{
    "buyerId": "buyer-123",
    "supplierId": "supplier-456",
    "items": [{
      "productId": "product-789",
      "productName": "Wheat",
      "quantity": 100.00,
      "price": 25.50,
      "unit": "kg"
    }]
  }'

# Confirm order
curl -X POST http://localhost:8081/api/v1/orders/{orderId}/confirm

# Cancel order
curl -X POST http://localhost:8081/api/v1/orders/{orderId}/cancel \
  -H "Content-Type: application/json" \
  -d '{"reason": "Cancelled by buyer"}'
```

## Monitoring

### Health Check
```bash
curl http://localhost:8081/actuator/health
```

### Metrics
```bash
curl http://localhost:8081/actuator/metrics
curl http://localhost:8081/actuator/prometheus
```

## Event Publishing

Events are published to Kafka topic: `procurement.procurementorder.events`

Event format:
```json
{
  "eventId": "event-uuid",
  "occurredOn": "2024-01-18T10:00:00Z",
  "aggregateId": "order-uuid",
  "version": 0,
  "eventType": "OrderCreatedEvent",
  "orderId": "order-uuid",
  "buyerId": "buyer-uuid",
  "supplierId": "supplier-uuid",
  "items": [...],
  "totalAmount": 999.90
}
```

## Security Considerations

- Database credentials externalized to environment variables
- SQL logging disabled in production (use dev profile for debugging)
- Rate limiting prevents abuse
- Idempotency prevents duplicate charges
- Input validation on all endpoints
- No sensitive data in logs or responses

## Performance Optimizations

- Batch processing of outbox events
- Connection pooling (HikariCP)
- JPA batch inserts/updates
- Indexed database queries
- Rate limiter cache with size limit
- Async Kafka publishing

## License

Copyright © 2024 Agri Platform
