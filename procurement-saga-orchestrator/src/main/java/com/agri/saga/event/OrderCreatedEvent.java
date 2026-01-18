package com.agri.saga.event;

import com.agri.common.event.DomainEvent;

import java.math.BigDecimal;
import java.util.List;

public class OrderCreatedEvent extends DomainEvent {
    
    private String orderId;
    private String buyerId;
    private BigDecimal totalAmount;
    private List<OrderItem> items;
    
    public OrderCreatedEvent() {
        super();
    }
    
    public OrderCreatedEvent(String orderId, String buyerId, BigDecimal totalAmount, List<OrderItem> items) {
        super(orderId, 1L);
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.totalAmount = totalAmount;
        this.items = items;
    }
    
    @Override
    public String getEventType() {
        return "OrderCreated";
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getBuyerId() {
        return buyerId;
    }
    
    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public static class OrderItem {
        private String productId;
        private Integer quantity;
        
        public OrderItem() {}
        
        public OrderItem(String productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        public String getProductId() {
            return productId;
        }
        
        public void setProductId(String productId) {
            this.productId = productId;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
