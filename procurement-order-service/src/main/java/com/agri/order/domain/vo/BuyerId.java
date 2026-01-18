package com.agri.order.domain.vo;

public record BuyerId(String value) {
    public BuyerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("BuyerId cannot be null or empty");
        }
    }
}
