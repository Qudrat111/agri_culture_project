package com.agri.order.domain.vo;

public record SupplierId(String value) {
    public SupplierId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SupplierId cannot be null or empty");
        }
    }
}
