package com.agri.order.domain.event;

import com.agri.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderConfirmedEvent extends DomainEvent {
    
    private String orderId;
    
    public OrderConfirmedEvent(String orderId, Long version) {
        super(orderId, version);
        this.orderId = orderId;
    }
    
    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
