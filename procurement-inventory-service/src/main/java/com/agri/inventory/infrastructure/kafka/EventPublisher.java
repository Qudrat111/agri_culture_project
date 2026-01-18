package com.agri.inventory.infrastructure.kafka;

import com.agri.inventory.event.InventoryReservationFailedEvent;
import com.agri.inventory.event.InventoryReservedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    
    private static final String INVENTORY_EVENTS_TOPIC = "inventory.events";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Publish inventory reserved event.
     */
    public void publishInventoryReservedEvent(InventoryReservedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            log.info("Publishing InventoryReservedEvent for order: {}", event.getOrderId());
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, event.getOrderId(), eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published InventoryReservedEvent for order: {} to partition: {}", 
                        event.getOrderId(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to publish InventoryReservedEvent for order: {}", 
                        event.getOrderId(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing InventoryReservedEvent for order: {}", 
                event.getOrderId(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
    /**
     * Publish inventory reservation failed event.
     */
    public void publishInventoryReservationFailedEvent(InventoryReservationFailedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            log.info("Publishing InventoryReservationFailedEvent for order: {} - Reason: {}", 
                event.getOrderId(), event.getReason());
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, event.getOrderId(), eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published InventoryReservationFailedEvent for order: {} to partition: {}", 
                        event.getOrderId(), result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to publish InventoryReservationFailedEvent for order: {}", 
                        event.getOrderId(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing InventoryReservationFailedEvent for order: {}", 
                event.getOrderId(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
