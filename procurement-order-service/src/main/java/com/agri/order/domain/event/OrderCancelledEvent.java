package com.agri.order.domain.event;

import com.agri.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCancelledEvent extends DomainEvent {
    
    private String orderId;
    private String reason;
    
    public OrderCancelledEvent(String orderId, Long version, String reason) {
        super(orderId, version);
        this.orderId = orderId;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
