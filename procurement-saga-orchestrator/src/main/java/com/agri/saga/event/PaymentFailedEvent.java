package com.agri.saga.event;

import com.agri.common.event.DomainEvent;

public class PaymentFailedEvent extends DomainEvent {
    
    private String orderId;
    private String reason;
    
    public PaymentFailedEvent() {
        super();
    }
    
    public PaymentFailedEvent(String orderId, String reason) {
        super(orderId, 1L);
        this.orderId = orderId;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "PaymentFailed";
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
