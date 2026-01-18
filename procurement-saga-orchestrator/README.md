# Procurement Saga Orchestrator

This service orchestrates distributed transactions for the Digital Procurement Platform using the SAGA pattern.

## Overview

The Saga Orchestrator coordinates multi-step distributed transactions across Inventory, Payment, and Order services. It ensures data consistency through compensating transactions in case of failures.

## Architecture

### SAGA Flow

```
OrderCreatedEvent
    ↓
[RESERVE_INVENTORY] → ReserveInventoryCommand
    ↓
InventoryReservedEvent
    ↓
[PROCESS_PAYMENT] → ProcessPaymentCommand
    ↓
PaymentProcessedEvent
    ↓
[CONFIRM_ORDER] → ConfirmOrderCommand
    ↓
OrderConfirmedEvent
    ↓
[COMPLETED]
```

### Compensation Flow

If any step fails, the saga initiates compensating transactions:

- **Payment Fails**: Release reserved inventory
- **Inventory Reservation Fails**: Mark saga as failed (no compensation needed)

## Components

### Domain

- **ProcurementSaga**: Entity representing the saga state
- **SagaStatus**: STARTED, PROCESSING, COMPLETED, COMPENSATING, FAILED
- **SagaStep**: RESERVE_INVENTORY, PROCESS_PAYMENT, CONFIRM_ORDER, COMPLETED

### Application

- **SagaOrchestrator**: Main orchestration logic
  - Handles all event transitions
  - Publishes commands to downstream services
  - Manages compensation flows
  - Uses MDC for distributed tracing (sagaId, orderId)

### Infrastructure

- **SagaRepository**: JPA repository for saga persistence
- **EventListener**: Kafka consumer for events from all services
  - Retry logic with exponential backoff (3 attempts, 1s initial delay, 2x multiplier)
- **CommandPublisher**: Kafka producer for commands
  - Idempotent producer with acks=all

### Commands

- `ReserveInventoryCommand`: Reserve inventory items
- `ProcessPaymentCommand`: Process payment for order
- `ConfirmOrderCommand`: Confirm order completion
- `CompensateInventoryCommand`: Rollback inventory reservation

## Kafka Topics

### Consumed Events

- `procurement.procurementorder.events`: OrderCreatedEvent
- `inventory.events`: InventoryReservedEvent, InventoryReservationFailedEvent
- `payment.events`: PaymentProcessedEvent, PaymentFailedEvent
- `order.events`: OrderConfirmedEvent

### Published Commands

- `inventory.commands`: ReserveInventoryCommand, CompensateInventoryCommand
- `payment.commands`: ProcessPaymentCommand
- `order.commands`: ConfirmOrderCommand

## Configuration

### Database

PostgreSQL database with Flyway migrations:
- URL: `jdbc:postgresql://localhost:5432/agri_procurement`
- Schema: `procurement_sagas` table

### Kafka

- Bootstrap servers: `localhost:9092`
- Consumer group: `saga-orchestrator-group`
- Producer: Idempotent with `acks=all`

### Ports

- Application: `8082`

## Features

1. **Distributed Transaction Coordination**: Orchestrates multi-service transactions
2. **Compensation Handling**: Automatic rollback on failures
3. **Idempotency**: Prevents duplicate saga creation for same order
4. **Retry Mechanism**: Automatic retry with exponential backoff on Kafka consumers
5. **Optimistic Locking**: Version control on saga entity
6. **MDC Logging**: Structured logging with sagaId and orderId
7. **Observability**: OpenTelemetry tracing and Prometheus metrics

## Running the Service

### Prerequisites

- Java 21
- PostgreSQL running on port 5432
- Kafka running on port 9092

### Build

```bash
./gradlew :procurement-saga-orchestrator:build
```

### Run

```bash
./gradlew :procurement-saga-orchestrator:bootRun
```

### Docker Compose

The service can be started as part of the platform:

```bash
docker-compose up -d
```

## Monitoring

### Health Check

```bash
curl http://localhost:8082/actuator/health
```

### Metrics

```bash
curl http://localhost:8082/actuator/metrics
curl http://localhost:8082/actuator/prometheus
```

## Error Handling

1. **Transient Failures**: Kafka consumers retry 3 times with exponential backoff
2. **Business Failures**: Saga marked as FAILED with reason stored
3. **Compensation**: Automatic rollback of completed steps
4. **Duplicate Prevention**: Idempotency checks for existing sagas

## Development Notes

- Uses Spring Boot 3.3.7 and Java 21 records
- Follows Domain-Driven Design principles
- Implements SAGA orchestration pattern
- All event handlers are transactional
- MDC context for distributed tracing
- Version-controlled entities for concurrency control
