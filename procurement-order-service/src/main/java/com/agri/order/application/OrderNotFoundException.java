package com.agri.order.application;

import com.agri.common.exception.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {
    
    public OrderNotFoundException(String orderId) {
        super("Order not found with id: " + orderId);
    }
}
