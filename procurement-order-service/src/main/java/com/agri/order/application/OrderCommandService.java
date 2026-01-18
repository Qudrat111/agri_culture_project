package com.agri.order.application;

import com.agri.common.event.DomainEvent;
import com.agri.common.idempotency.IdempotencyKey;
import com.agri.common.outbox.OutboxEvent;
import com.agri.order.api.CreateOrderRequest;
import com.agri.order.api.OrderResponse;
import com.agri.order.domain.ProcurementOrder;
import com.agri.order.domain.vo.BuyerId;
import com.agri.order.domain.vo.SupplierId;
import com.agri.order.infrastructure.repository.IdempotencyKeyRepository;
import com.agri.order.infrastructure.repository.OrderRepository;
import com.agri.order.infrastructure.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCommandService {
    
    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String idempotencyKey) {
        log.info("Creating order with idempotency key: {}", idempotencyKey);
        
        // Check idempotency
        var existingKey = idempotencyKeyRepository.findById(idempotencyKey);
        if (existingKey.isPresent()) {
            if (!existingKey.get().isExpired()) {
                log.info("Idempotency key found, returning cached response");
                try {
                    return objectMapper.readValue(existingKey.get().getResponse(), OrderResponse.class);
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize cached response", e);
                    throw new RuntimeException("Failed to process idempotent request", e);
                }
            } else {
                // Remove expired key
                idempotencyKeyRepository.delete(existingKey.get());
            }
        }
        
        // Create new order
        String orderId = UUID.randomUUID().toString();
        MDC.put("orderId", orderId);
        
        ProcurementOrder order = new ProcurementOrder(
            orderId,
            new BuyerId(request.buyerId()),
            new SupplierId(request.supplierId()),
            request.items()
        );
        
        // Save order
        orderRepository.save(order);
        log.info("Order created with id: {}", orderId);
        
        // Save domain events to outbox
        for (DomainEvent event : order.getDomainEvents()) {
            saveEventToOutbox(event);
        }
        order.clearDomainEvents();
        
        // Create response
        OrderResponse response = OrderResponse.from(order);
        
        // Save idempotency key
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            IdempotencyKey key = new IdempotencyKey(idempotencyKey, responseJson);
            idempotencyKeyRepository.save(key);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response for idempotency", e);
            throw new RuntimeException("Failed to save idempotency key", e);
        }
        
        MDC.remove("orderId");
        return response;
    }
    
    @Transactional
    public OrderResponse confirmOrder(String orderId) {
        log.info("Confirming order: {}", orderId);
        MDC.put("orderId", orderId);
        
        ProcurementOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        order.confirm();
        orderRepository.save(order);
        
        // Save domain events to outbox
        for (DomainEvent event : order.getDomainEvents()) {
            saveEventToOutbox(event);
        }
        order.clearDomainEvents();
        
        log.info("Order confirmed: {}", orderId);
        MDC.remove("orderId");
        return OrderResponse.from(order);
    }
    
    @Transactional
    public OrderResponse cancelOrder(String orderId, String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);
        MDC.put("orderId", orderId);
        
        ProcurementOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        order.cancel(reason);
        orderRepository.save(order);
        
        // Save domain events to outbox
        for (DomainEvent event : order.getDomainEvents()) {
            saveEventToOutbox(event);
        }
        order.clearDomainEvents();
        
        log.info("Order cancelled: {}", orderId);
        MDC.remove("orderId");
        return OrderResponse.from(order);
    }
    
    private void saveEventToOutbox(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                event.getEventId(),
                "ProcurementOrder",
                event.getAggregateId(),
                event.getEventType(),
                payload
            );
            outboxEventRepository.save(outboxEvent);
            log.debug("Event saved to outbox: {} for aggregate: {}", 
                event.getEventType(), event.getAggregateId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize domain event", e);
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
}
