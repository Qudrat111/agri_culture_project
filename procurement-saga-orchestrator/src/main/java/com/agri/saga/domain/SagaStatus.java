package com.agri.saga.domain;

public enum SagaStatus {
    STARTED,
    PROCESSING,
    COMPLETED,
    COMPENSATING,
    FAILED
}
