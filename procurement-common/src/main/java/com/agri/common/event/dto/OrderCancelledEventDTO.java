package com.agri.common.event.dto;

import com.agri.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCancelledEventDTO extends DomainEvent {
    
    private String orderId;
    private String reason;
    
    public OrderCancelledEventDTO(String orderId, Long version, String reason) {
        super(orderId, version);
        this.orderId = orderId;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "OrderCancelledEvent";
    }
}
