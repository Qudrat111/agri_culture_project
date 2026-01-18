package com.agri.saga.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "procurement_sagas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementSaga {
    
    @Id
    private String id;
    
    @Column(name = "order_id", nullable = false)
    private String orderId;
    
    @Column(name = "buyer_id")
    private String buyerId;
    
    @Column(name = "total_amount")
    private java.math.BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    private SagaStep currentStep;
    
    @Column(name = "inventory_reserved")
    private Boolean inventoryReserved = false;
    
    @Column(name = "payment_processed")
    private Boolean paymentProcessed = false;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Version
    private Long version;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    public void moveToNextStep() {
        this.status = SagaStatus.PROCESSING;
        switch (currentStep) {
            case RESERVE_INVENTORY:
                this.currentStep = SagaStep.PROCESS_PAYMENT;
                break;
            case PROCESS_PAYMENT:
                this.currentStep = SagaStep.CONFIRM_ORDER;
                break;
            case CONFIRM_ORDER:
                this.currentStep = SagaStep.COMPLETED;
                break;
            default:
                break;
        }
    }
    
    public void startCompensation() {
        this.status = SagaStatus.COMPENSATING;
    }
    
    public void fail(String reason) {
        this.status = SagaStatus.FAILED;
        this.failureReason = reason;
    }
    
    public void complete() {
        this.status = SagaStatus.COMPLETED;
        this.currentStep = SagaStep.COMPLETED;
    }
    
    public static ProcurementSaga create(String sagaId, String orderId, String buyerId, java.math.BigDecimal totalAmount) {
        ProcurementSaga saga = new ProcurementSaga();
        saga.setId(sagaId);
        saga.setOrderId(orderId);
        saga.setBuyerId(buyerId);
        saga.setTotalAmount(totalAmount);
        saga.setStatus(SagaStatus.STARTED);
        saga.setCurrentStep(SagaStep.RESERVE_INVENTORY);
        saga.setInventoryReserved(false);
        saga.setPaymentProcessed(false);
        return saga;
    }
}
