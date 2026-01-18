package com.agri.saga.infrastructure.kafka;

import com.agri.saga.application.SagaOrchestrator;
import com.agri.saga.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventListener {
    
    private final SagaOrchestrator sagaOrchestrator;
    
    public EventListener(SagaOrchestrator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }
    
    @KafkaListener(
        topics = "procurement.procurementorder.events",
        groupId = "saga-orchestrator-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for orderId: {}", event.getOrderId());
        try {
            sagaOrchestrator.startSaga(event);
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for orderId: {}", event.getOrderId(), e);
            throw e;
        }
    }
    
    @KafkaListener(
        topics = "inventory.events",
        groupId = "saga-orchestrator-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleInventoryEvents(Object event) {
        log.debug("Received inventory event: {}", event.getClass().getSimpleName());
        
        try {
            if (event instanceof InventoryReservedEvent inventoryReservedEvent) {
                log.info("Received InventoryReservedEvent for orderId: {}", inventoryReservedEvent.getOrderId());
                sagaOrchestrator.handleInventoryReserved(inventoryReservedEvent);
            } else if (event instanceof InventoryReservationFailedEvent failedEvent) {
                log.info("Received InventoryReservationFailedEvent for orderId: {}", failedEvent.getOrderId());
                sagaOrchestrator.handleInventoryReservationFailed(failedEvent);
            } else {
                log.debug("Ignoring unhandled inventory event type: {}", event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error processing inventory event", e);
            throw e;
        }
    }
    
    @KafkaListener(
        topics = "payment.events",
        groupId = "saga-orchestrator-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handlePaymentEvents(Object event) {
        log.debug("Received payment event: {}", event.getClass().getSimpleName());
        
        try {
            if (event instanceof PaymentProcessedEvent paymentProcessedEvent) {
                log.info("Received PaymentProcessedEvent for orderId: {}", paymentProcessedEvent.getOrderId());
                sagaOrchestrator.handlePaymentProcessed(paymentProcessedEvent);
            } else if (event instanceof PaymentFailedEvent failedEvent) {
                log.info("Received PaymentFailedEvent for orderId: {}", failedEvent.getOrderId());
                sagaOrchestrator.handlePaymentFailed(failedEvent);
            } else {
                log.debug("Ignoring unhandled payment event type: {}", event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error processing payment event", e);
            throw e;
        }
    }
    
    @KafkaListener(
        topics = "order.events",
        groupId = "saga-orchestrator-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleOrderEvents(Object event) {
        log.debug("Received order event: {}", event.getClass().getSimpleName());
        
        try {
            if (event instanceof OrderConfirmedEvent orderConfirmedEvent) {
                log.info("Received OrderConfirmedEvent for orderId: {}", orderConfirmedEvent.getOrderId());
                sagaOrchestrator.handleOrderConfirmed(orderConfirmedEvent);
            } else {
                log.debug("Ignoring unhandled order event type: {}", event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error processing order event", e);
            throw e;
        }
    }
}
