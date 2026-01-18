package com.agri.order.api;

import com.agri.order.application.OrderCommandService;
import com.agri.order.application.OrderNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    private static final String CLIENT_ID_HEADER = "X-Client-Id";
    
    private final OrderCommandService orderCommandService;
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @RequestHeader(CLIENT_ID_HEADER) String clientId) {
        
        log.info("Received create order request from client: {}", clientId);
        
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        OrderResponse response = orderCommandService.createOrder(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable String orderId) {
        log.info("Received confirm order request for orderId: {}", orderId);
        
        try {
            OrderResponse response = orderCommandService.confirmOrder(orderId);
            return ResponseEntity.ok(response);
        } catch (OrderNotFoundException e) {
            log.error("Order not found: {}", orderId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Invalid state transition for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> requestBody) {
        
        log.info("Received cancel order request for orderId: {}", orderId);
        
        String reason = requestBody.get("reason");
        if (reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            OrderResponse response = orderCommandService.cancelOrder(orderId, reason);
            return ResponseEntity.ok(response);
        } catch (OrderNotFoundException e) {
            log.error("Order not found: {}", orderId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Invalid state transition for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "An unexpected error occurred"));
    }
}
