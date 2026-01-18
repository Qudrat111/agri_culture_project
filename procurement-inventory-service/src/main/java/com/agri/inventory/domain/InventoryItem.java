package com.agri.inventory.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    
    @Id
    private String id;
    
    @Column(name = "product_id", nullable = false, unique = true)
    private String productId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;
    
    @Version
    private Long version;
    
    /**
     * Check if the requested quantity is available in stock.
     */
    public boolean hasAvailableQuantity(Integer quantity) {
        return availableQuantity >= quantity;
    }
    
    /**
     * Reserve the specified quantity from available stock.
     * This decreases available quantity and increases reserved quantity.
     */
    public void reserve(Integer quantity) {
        if (!hasAvailableQuantity(quantity)) {
            throw new IllegalStateException(
                String.format("Insufficient inventory for product %s. Available: %d, Requested: %d",
                    productId, availableQuantity, quantity)
            );
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }
    
    /**
     * Release the specified quantity back to available stock.
     * This increases available quantity and decreases reserved quantity.
     */
    public void release(Integer quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException(
                String.format("Cannot release more than reserved for product %s. Reserved: %d, Release requested: %d",
                    productId, reservedQuantity, quantity)
            );
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }
}
