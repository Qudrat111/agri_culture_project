package com.agri.query.api;

import com.agri.query.model.OrderView;
import com.agri.query.repository.OrderViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderQueryController {

    private final OrderViewRepository orderViewRepository;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderViewResponse> getOrderById(@PathVariable String orderId) {
        MDC.put("orderId", orderId);
        
        try {
            log.info("Fetching order by ID: {}", orderId);
            
            return orderViewRepository.findById(orderId)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Order not found: {}", orderId);
                    return ResponseEntity.notFound().build();
                });
                
        } catch (Exception e) {
            log.error("Error fetching order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            MDC.remove("orderId");
        }
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<OrderViewResponse>> getOrdersByBuyer(
        @PathVariable String buyerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            log.info("Fetching orders for buyer: {}, page: {}, size: {}", buyerId, page, size);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<OrderView> orders = orderViewRepository.findByBuyerId(buyerId, pageable);
            
            List<OrderViewResponse> responses = orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
            
            log.info("Found {} orders for buyer: {}", responses.size(), buyerId);
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error fetching orders for buyer: {}", buyerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<OrderViewResponse>> getOrdersBySupplier(
        @PathVariable String supplierId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            log.info("Fetching orders for supplier: {}, page: {}, size: {}", supplierId, page, size);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<OrderView> orders = orderViewRepository.findBySupplierId(supplierId, pageable);
            
            List<OrderViewResponse> responses = orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
            
            log.info("Found {} orders for supplier: {}", responses.size(), supplierId);
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error fetching orders for supplier: {}", supplierId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderViewResponse>> getAllOrders(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            log.info("Fetching all orders with status: {}, page: {}, size: {}", status, page, size);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<OrderView> orders;
            
            if (status != null && !status.isBlank()) {
                orders = orderViewRepository.findByStatus(status.toUpperCase(), pageable);
            } else {
                orders = orderViewRepository.findAllBy(pageable);
            }
            
            List<OrderViewResponse> responses = orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
            
            log.info("Found {} orders", responses.size());
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error fetching orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private OrderViewResponse mapToResponse(OrderView orderView) {
        return OrderViewResponse.builder()
            .id(orderView.getId())
            .buyerId(orderView.getBuyerId())
            .buyerName(orderView.getBuyerName())
            .supplierId(orderView.getSupplierId())
            .supplierName(orderView.getSupplierName())
            .items(orderView.getItems())
            .status(orderView.getStatus())
            .totalAmount(orderView.getTotalAmount())
            .createdAt(orderView.getCreatedAt())
            .updatedAt(orderView.getUpdatedAt())
            .build();
    }
}
