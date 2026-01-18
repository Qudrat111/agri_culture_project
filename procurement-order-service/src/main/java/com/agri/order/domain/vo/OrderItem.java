package com.agri.order.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @NotBlank(message = "Product ID is required")
    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;
    
    @NotBlank(message = "Product name is required")
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    
    @NotBlank(message = "Unit is required")
    @Column(name = "unit", nullable = false, length = 50)
    private String unit;
    
    public BigDecimal getSubtotal() {
        return quantity.multiply(price);
    }
}
