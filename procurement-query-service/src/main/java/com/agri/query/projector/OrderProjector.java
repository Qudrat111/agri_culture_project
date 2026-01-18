package com.agri.query.projector;

import com.agri.common.event.DomainEvent;
import com.agri.common.event.dto.OrderCancelledEventDTO;
import com.agri.common.event.dto.OrderCompletedEventDTO;
import com.agri.common.event.dto.OrderConfirmedEventDTO;
import com.agri.common.event.dto.OrderCreatedEventDTO;
import com.agri.common.vo.OrderItem;
import com.agri.query.model.OrderItemView;
import com.agri.query.model.OrderView;
import com.agri.query.repository.OrderViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(
    topics = "procurement.procurementorder.events",
    groupId = "query-projector-group",
    containerFactory = "kafkaListenerContainerFactory"
)
public class OrderProjector {

    // TODO: Replace with actual name resolution from User Service in production
    // These prefixes create placeholder names until proper service integration
    private static final String BUYER_NAME_PREFIX = "Buyer-";
    private static final String SUPPLIER_NAME_PREFIX = "Supplier-";

    private final OrderViewRepository orderViewRepository;

    @KafkaHandler
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleOrderCreated(OrderCreatedEventDTO event) {
        String orderId = event.getOrderId();
        MDC.put("orderId", orderId);
        
        try {
            log.info("Processing OrderCreatedEvent for order: {}", orderId);
            
            OrderView orderView = OrderView.builder()
                .id(orderId)
                .buyerId(event.getBuyerId())
                .buyerName(BUYER_NAME_PREFIX + event.getBuyerId())
                .supplierId(event.getSupplierId())
                .supplierName(SUPPLIER_NAME_PREFIX + event.getSupplierId())
                .items(mapToOrderItemViews(event.getItems()))
                .status("PENDING")
                .totalAmount(event.getTotalAmount())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
            
            orderViewRepository.save(orderView);
            log.info("Successfully created OrderView for order: {}", orderId);
            
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for order: {}", orderId, e);
            throw e;
        } finally {
            MDC.remove("orderId");
        }
    }

    @KafkaHandler
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleOrderConfirmed(OrderConfirmedEventDTO event) {
        String orderId = event.getOrderId();
        MDC.put("orderId", orderId);
        
        try {
            log.info("Processing OrderConfirmedEvent for order: {}", orderId);
            
            orderViewRepository.findById(orderId).ifPresentOrElse(
                orderView -> {
                    orderView.setStatus("CONFIRMED");
                    orderView.setUpdatedAt(Instant.now());
                    orderViewRepository.save(orderView);
                    log.info("Successfully updated OrderView status to CONFIRMED for order: {}", orderId);
                },
                () -> log.warn("OrderView not found for order: {}", orderId)
            );
            
        } catch (Exception e) {
            log.error("Error processing OrderConfirmedEvent for order: {}", orderId, e);
            throw e;
        } finally {
            MDC.remove("orderId");
        }
    }

    @KafkaHandler
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleOrderCancelled(OrderCancelledEventDTO event) {
        String orderId = event.getOrderId();
        MDC.put("orderId", orderId);
        
        try {
            log.info("Processing OrderCancelledEvent for order: {}", orderId);
            
            orderViewRepository.findById(orderId).ifPresentOrElse(
                orderView -> {
                    orderView.setStatus("CANCELLED");
                    orderView.setUpdatedAt(Instant.now());
                    orderViewRepository.save(orderView);
                    log.info("Successfully updated OrderView status to CANCELLED for order: {}", orderId);
                },
                () -> log.warn("OrderView not found for order: {}", orderId)
            );
            
        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent for order: {}", orderId, e);
            throw e;
        } finally {
            MDC.remove("orderId");
        }
    }

    @KafkaHandler
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleOrderCompleted(OrderCompletedEventDTO event) {
        String orderId = event.getOrderId();
        MDC.put("orderId", orderId);
        
        try {
            log.info("Processing OrderCompletedEvent for order: {}", orderId);
            
            orderViewRepository.findById(orderId).ifPresentOrElse(
                orderView -> {
                    orderView.setStatus("COMPLETED");
                    orderView.setUpdatedAt(Instant.now());
                    orderViewRepository.save(orderView);
                    log.info("Successfully updated OrderView status to COMPLETED for order: {}", orderId);
                },
                () -> log.warn("OrderView not found for order: {}", orderId)
            );
            
        } catch (Exception e) {
            log.error("Error processing OrderCompletedEvent for order: {}", orderId, e);
            throw e;
        } finally {
            MDC.remove("orderId");
        }
    }

    private List<OrderItemView> mapToOrderItemViews(List<OrderItem> items) {
        return items.stream()
            .map(item -> new OrderItemView(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                item.getUnit()
            ))
            .collect(Collectors.toList());
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("Received unknown event type: {}", event.getClass().getSimpleName());
    }
}
