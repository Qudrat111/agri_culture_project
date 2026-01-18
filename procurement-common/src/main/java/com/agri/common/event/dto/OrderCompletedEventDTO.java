package com.agri.common.event.dto;

import com.agri.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCompletedEventDTO extends DomainEvent {
    
    private String orderId;
    
    public OrderCompletedEventDTO(String orderId, Long version) {
        super(orderId, version);
        this.orderId = orderId;
    }
    
    @Override
    public String getEventType() {
        return "OrderCompletedEvent";
    }
}
