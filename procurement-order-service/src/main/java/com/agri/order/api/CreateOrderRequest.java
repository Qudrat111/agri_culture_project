package com.agri.order.api;

import com.agri.order.domain.vo.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
    @NotBlank(message = "Buyer ID is required")
    String buyerId,
    
    @NotBlank(message = "Supplier ID is required")
    String supplierId,
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    List<OrderItem> items
) {
}
