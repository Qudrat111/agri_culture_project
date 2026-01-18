# Procurement Inventory Service

## Overview
The Procurement Inventory Service is responsible for managing inventory reservations as part of the SAGA orchestration pattern in the Digital Procurement Platform. It handles inventory availability checks, reservations, and compensations (releases) for order processing.

## Architecture

### Components

#### Domain Layer
- **InventoryItem**: Entity representing product inventory with available and reserved quantities
  - Supports optimistic locking via `@Version`
  - Methods: `reserve()`, `release()`, `hasAvailableQuantity()`
- **Reservation**: Entity tracking reserved inventory for specific orders
  - Links order ID to product ID and quantity
  - Timestamped for audit purposes

#### Application Layer
- **InventoryService**: Core business logic
  - `reserveInventory()`: Processes inventory reservation requests
  - `releaseInventory()`: Handles SAGA compensation (rollback)
  - Uses pessimistic locking for concurrent access control
  - Implements idempotency checks

#### Infrastructure Layer
- **Repositories**:
  - `InventoryItemRepository`: JPA repository with pessimistic write locking
  - `ReservationRepository`: Manages reservation records
- **Kafka Components**:
  - `CommandListener`: Consumes inventory commands from SAGA orchestrator
  - `EventPublisher`: Publishes inventory events
  - `KafkaConfig`: Kafka producer/consumer configuration

#### Events
- **InventoryReservedEvent**: Published when inventory is successfully reserved
- **InventoryReservationFailedEvent**: Published when reservation fails (insufficient stock, product not found)

## Key Features

### 1. Pessimistic Locking
Uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` to prevent concurrent modifications and ensure data consistency during high-volume operations.

### 2. Idempotency
Checks for existing reservations before processing to handle duplicate commands safely.

### 3. Retry Mechanism
- `@Retryable` with exponential backoff (3 attempts, 1s delay, 2x multiplier)
- Ensures transient failures don't cause data loss

### 4. SAGA Compensation
Supports order rollback by releasing reserved inventory back to available stock.

### 5. MDC Logging
Contextual logging with order ID and command type for traceability.

## Configuration

### Database
- PostgreSQL with Flyway migrations
- Schema initialization with sample data
- Connection pooling via HikariCP

### Kafka
- Consumer Group: `inventory-service-group`
- Topics:
  - Consumes: `inventory.commands`
  - Produces: `inventory.events`
- Idempotent producer with `acks=all`
- Read committed isolation level

### Port
- Default: `8084`

## Database Schema

### inventory_items
```sql
- id (VARCHAR 36, PK)
- product_id (VARCHAR 36, UNIQUE)
- product_name (VARCHAR 255)
- available_quantity (INT)
- reserved_quantity (INT)
- version (BIGINT) -- Optimistic locking
```

### reservations
```sql
- id (VARCHAR 36, PK)
- order_id (VARCHAR 36)
- product_id (VARCHAR 36)
- quantity (INT)
- created_at (TIMESTAMP)
- version (BIGINT)
```

## Sample Data
Pre-loaded inventory:
- Organic Wheat (100kg): 1000 units
- Rice Seeds (50kg): 500 units
- Fertilizer NPK (25kg): 750 units

## SAGA Flow

### Happy Path
1. Receive `ReserveInventoryCommand`
2. Validate product exists and quantity available
3. Create reservation records
4. Deduct from available quantity
5. Publish `InventoryReservedEvent`

### Failure Path
1. Receive `ReserveInventoryCommand`
2. Detect insufficient inventory or missing product
3. Publish `InventoryReservationFailedEvent`
4. SAGA orchestrator initiates rollback

### Compensation
1. Receive `CompensateInventoryCommand`
2. Find reservations by order ID
3. Add quantities back to available stock
4. Delete reservation records

## Error Handling
- Insufficient inventory: Publishes failure event with specific reason
- Product not found: Publishes failure event
- Database errors: Logged and failure event published
- Retry exhaustion: Error logged and propagated

## Observability
- Actuator endpoints: health, metrics, prometheus
- Micrometer tracing with OpenTelemetry
- Structured logging with MDC context
- SQL logging for debugging

## Running the Service

### Standalone
```bash
./gradlew :procurement-inventory-service:bootRun
```

### Docker Compose
```bash
docker-compose up inventory-service
```

## Dependencies
- Spring Boot 3.3
- Spring Data JPA
- Spring Kafka
- PostgreSQL + Flyway
- Spring Retry
- Lombok
- Micrometer Tracing
- procurement-common module
