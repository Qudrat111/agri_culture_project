package com.agri.order.api;

import com.agri.order.domain.OrderStatus;
import com.agri.order.domain.ProcurementOrder;
import com.agri.order.domain.vo.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
    String id,
    String buyerId,
    String supplierId,
    List<OrderItem> items,
    OrderStatus status,
    BigDecimal totalAmount,
    Instant createdAt,
    Instant updatedAt
) {
    public static OrderResponse from(ProcurementOrder order) {
        return new OrderResponse(
            order.getId(),
            order.getBuyerId(),
            order.getSupplierId(),
            order.getItems(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
