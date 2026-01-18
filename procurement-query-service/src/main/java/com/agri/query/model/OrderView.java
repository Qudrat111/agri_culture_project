package com.agri.query.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "order_views")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderView {
    
    @Id
    private String id;
    
    @Indexed
    private String buyerId;
    
    private String buyerName;
    
    @Indexed
    private String supplierId;
    
    private String supplierName;
    
    private List<OrderItemView> items;
    
    @Indexed
    private String status;
    
    private BigDecimal totalAmount;
    
    @Indexed
    private Instant createdAt;
    
    private Instant updatedAt;
}
