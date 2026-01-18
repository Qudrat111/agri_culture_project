package com.agri.saga.event;

import com.agri.common.event.DomainEvent;

public class PaymentProcessedEvent extends DomainEvent {
    
    private String orderId;
    
    public PaymentProcessedEvent() {
        super();
    }
    
    public PaymentProcessedEvent(String orderId) {
        super(orderId, 1L);
        this.orderId = orderId;
    }
    
    @Override
    public String getEventType() {
        return "PaymentProcessed";
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
