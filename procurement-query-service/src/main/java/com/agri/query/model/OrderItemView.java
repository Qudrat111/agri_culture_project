package com.agri.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemView {
    
    private String productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal price;
    private String unit;
    
    public BigDecimal getSubtotal() {
        return quantity.multiply(price);
    }
}
