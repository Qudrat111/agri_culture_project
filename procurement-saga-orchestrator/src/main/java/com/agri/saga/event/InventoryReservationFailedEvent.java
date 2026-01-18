package com.agri.saga.event;

import com.agri.common.event.DomainEvent;

public class InventoryReservationFailedEvent extends DomainEvent {
    
    private String orderId;
    private String reason;
    
    public InventoryReservationFailedEvent() {
        super();
    }
    
    public InventoryReservationFailedEvent(String orderId, String reason) {
        super(orderId, 1L);
        this.orderId = orderId;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "InventoryReservationFailed";
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
