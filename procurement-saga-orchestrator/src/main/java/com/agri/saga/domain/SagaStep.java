package com.agri.saga.domain;

public enum SagaStep {
    RESERVE_INVENTORY,
    PROCESS_PAYMENT,
    CONFIRM_ORDER,
    COMPLETED
}
