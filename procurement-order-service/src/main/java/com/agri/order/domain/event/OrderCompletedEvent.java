package com.agri.order.domain.event;

import com.agri.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCompletedEvent extends DomainEvent {
    
    private String orderId;
    
    public OrderCompletedEvent(String orderId, Long version) {
        super(orderId, version);
        this.orderId = orderId;
    }
    
    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
