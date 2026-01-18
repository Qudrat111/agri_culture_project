package com.agri.saga.event;

import com.agri.common.event.DomainEvent;

public class InventoryReservedEvent extends DomainEvent {
    
    private String orderId;
    
    public InventoryReservedEvent() {
        super();
    }
    
    public InventoryReservedEvent(String orderId) {
        super(orderId, 1L);
        this.orderId = orderId;
    }
    
    @Override
    public String getEventType() {
        return "InventoryReserved";
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
