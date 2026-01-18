package com.agri.common.command;

import java.util.List;

public record ReserveInventoryCommand(
    String orderId,
    List<LineItem> items
) {
    public record LineItem(
        String productId,
        Integer quantity
    ) {}
}
