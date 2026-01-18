package com.agri.query.api;

import com.agri.query.model.OrderItemView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderViewResponse {
    
    private String id;
    private String buyerId;
    private String buyerName;
    private String supplierId;
    private String supplierName;
    private List<OrderItemView> items;
    private String status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
}
