package com.agri.saga.event;

import com.agri.common.event.DomainEvent;

public class OrderConfirmedEvent extends DomainEvent {
    
    private String orderId;
    
    public OrderConfirmedEvent() {
        super();
    }
    
    public OrderConfirmedEvent(String orderId) {
        super(orderId, 1L);
        this.orderId = orderId;
    }
    
    @Override
    public String getEventType() {
        return "OrderConfirmed";
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
