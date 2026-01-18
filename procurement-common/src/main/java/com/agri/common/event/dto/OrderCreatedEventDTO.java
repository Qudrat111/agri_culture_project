package com.agri.common.event.dto;

import com.agri.common.event.DomainEvent;
import com.agri.common.vo.OrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderCreatedEventDTO extends DomainEvent {
    
    private String orderId;
    private String buyerId;
    private String supplierId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    
    public OrderCreatedEventDTO(String orderId, Long version, String buyerId, 
                           String supplierId, List<OrderItem> items, BigDecimal totalAmount) {
        super(orderId, version);
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.supplierId = supplierId;
        this.items = items;
        this.totalAmount = totalAmount;
    }
    
    @Override
    public String getEventType() {
        return "OrderCreatedEvent";
    }
}
