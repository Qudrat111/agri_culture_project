package com.agri.inventory.event;

import com.agri.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryReservationFailedEvent extends DomainEvent {
    
    private String orderId;
    private String reason;
    
    public InventoryReservationFailedEvent(String orderId, String reason) {
        super(orderId, 1L);
        this.orderId = orderId;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "InventoryReservationFailed";
    }
}
