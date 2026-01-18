package com.agri.inventory.event;

import com.agri.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InventoryReservedEvent extends DomainEvent {
    
    private String orderId;
    private List<ReservedItem> items;
    
    public InventoryReservedEvent(String orderId, List<ReservedItem> items) {
        super(orderId, 1L);
        this.orderId = orderId;
        this.items = items;
    }
    
    @Override
    public String getEventType() {
        return "InventoryReserved";
    }
    
    public record ReservedItem(
        String productId,
        Integer quantity
    ) {}
}
