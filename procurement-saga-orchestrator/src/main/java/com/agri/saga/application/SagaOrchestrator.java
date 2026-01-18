package com.agri.saga.application;

import com.agri.common.command.*;
import com.agri.saga.domain.ProcurementSaga;
import com.agri.saga.domain.SagaStatus;
import com.agri.saga.domain.SagaStep;
import com.agri.saga.event.*;
import com.agri.saga.infrastructure.kafka.CommandPublisher;
import com.agri.saga.infrastructure.repository.SagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SagaOrchestrator {
    
    private final SagaRepository sagaRepository;
    private final CommandPublisher commandPublisher;
    
    public SagaOrchestrator(SagaRepository sagaRepository, CommandPublisher commandPublisher) {
        this.sagaRepository = sagaRepository;
        this.commandPublisher = commandPublisher;
    }
    
    @Transactional
    public void startSaga(OrderCreatedEvent event) {
        String sagaId = UUID.randomUUID().toString();
        String orderId = event.getOrderId();
        
        MDC.put("sagaId", sagaId);
        MDC.put("orderId", orderId);
        
        try {
            log.info("Starting saga for orderId: {}", orderId);
            
            Optional<ProcurementSaga> existingSaga = sagaRepository.findByOrderId(orderId);
            if (existingSaga.isPresent()) {
                log.warn("Saga already exists for orderId: {}. Skipping duplicate.", orderId);
                return;
            }
            
            ProcurementSaga saga = ProcurementSaga.create(sagaId, orderId, event.getBuyerId(), event.getTotalAmount());
            sagaRepository.save(saga);
            
            log.info("Saga created with id: {} for orderId: {}", sagaId, orderId);
            
            List<ReserveInventoryCommand.LineItem> items = event.getItems().stream()
                .map(item -> new ReserveInventoryCommand.LineItem(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());
            
            ReserveInventoryCommand command = new ReserveInventoryCommand(orderId, items);
            commandPublisher.publishReserveInventoryCommand(command);
            
            log.info("Saga started successfully, RESERVE_INVENTORY command published");
        } finally {
            MDC.clear();
        }
    }
    
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        String orderId = event.getOrderId();
        
        ProcurementSaga saga = findSagaByOrderId(orderId);
        if (saga == null) return;
        
        MDC.put("sagaId", saga.getId());
        MDC.put("orderId", orderId);
        
        try {
            log.info("Handling inventory reserved for orderId: {}", orderId);
            
            if (saga.getCurrentStep() != SagaStep.RESERVE_INVENTORY) {
                log.warn("Saga is not in RESERVE_INVENTORY step. Current step: {}", saga.getCurrentStep());
                return;
            }
            
            saga.setInventoryReserved(true);
            saga.moveToNextStep();
            sagaRepository.save(saga);
            
            log.info("Inventory marked as reserved. Moving to PROCESS_PAYMENT step.");
            
            ProcessPaymentCommand command = new ProcessPaymentCommand(
                orderId, 
                saga.getBuyerId(),
                saga.getTotalAmount()
            );
            commandPublisher.publishProcessPaymentCommand(command);
            
            log.info("PROCESS_PAYMENT command published");
        } finally {
            MDC.clear();
        }
    }
    
    @Transactional
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        String orderId = event.getOrderId();
        
        ProcurementSaga saga = findSagaByOrderId(orderId);
        if (saga == null) return;
        
        MDC.put("sagaId", saga.getId());
        MDC.put("orderId", orderId);
        
        try {
            log.error("Inventory reservation failed for orderId: {}. Reason: {}", orderId, event.getReason());
            
            saga.startCompensation();
            saga.fail("Inventory reservation failed: " + event.getReason());
            sagaRepository.save(saga);
            
            log.info("Saga marked as FAILED. No compensation needed as no steps completed.");
        } finally {
            MDC.clear();
        }
    }
    
    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        String orderId = event.getOrderId();
        
        ProcurementSaga saga = findSagaByOrderId(orderId);
        if (saga == null) return;
        
        MDC.put("sagaId", saga.getId());
        MDC.put("orderId", orderId);
        
        try {
            log.info("Handling payment processed for orderId: {}", orderId);
            
            if (saga.getCurrentStep() != SagaStep.PROCESS_PAYMENT) {
                log.warn("Saga is not in PROCESS_PAYMENT step. Current step: {}", saga.getCurrentStep());
                return;
            }
            
            saga.setPaymentProcessed(true);
            saga.moveToNextStep();
            sagaRepository.save(saga);
            
            log.info("Payment marked as processed. Moving to CONFIRM_ORDER step.");
            
            ConfirmOrderCommand command = new ConfirmOrderCommand(orderId);
            commandPublisher.publishConfirmOrderCommand(command);
            
            log.info("CONFIRM_ORDER command published");
        } finally {
            MDC.clear();
        }
    }
    
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        String orderId = event.getOrderId();
        
        ProcurementSaga saga = findSagaByOrderId(orderId);
        if (saga == null) return;
        
        MDC.put("sagaId", saga.getId());
        MDC.put("orderId", orderId);
        
        try {
            log.error("Payment failed for orderId: {}. Reason: {}", orderId, event.getReason());
            
            saga.startCompensation();
            
            if (saga.getInventoryReserved()) {
                log.info("Starting compensation: releasing inventory");
                CompensateInventoryCommand command = new CompensateInventoryCommand(orderId);
                commandPublisher.publishCompensateInventoryCommand(command);
            }
            
            saga.fail("Payment failed: " + event.getReason());
            sagaRepository.save(saga);
            
            log.info("Saga marked as FAILED. Compensation initiated.");
        } finally {
            MDC.clear();
        }
    }
    
    @Transactional
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        String orderId = event.getOrderId();
        
        ProcurementSaga saga = findSagaByOrderId(orderId);
        if (saga == null) return;
        
        MDC.put("sagaId", saga.getId());
        MDC.put("orderId", orderId);
        
        try {
            log.info("Handling order confirmed for orderId: {}", orderId);
            
            if (saga.getCurrentStep() != SagaStep.CONFIRM_ORDER) {
                log.warn("Saga is not in CONFIRM_ORDER step. Current step: {}", saga.getCurrentStep());
                return;
            }
            
            saga.complete();
            sagaRepository.save(saga);
            
            log.info("Saga completed successfully for orderId: {}", orderId);
        } finally {
            MDC.clear();
        }
    }
    
    private ProcurementSaga findSagaByOrderId(String orderId) {
        return sagaRepository.findByOrderId(orderId)
            .filter(saga -> saga.getStatus() != SagaStatus.COMPLETED && saga.getStatus() != SagaStatus.FAILED)
            .orElseGet(() -> {
                log.error("No active saga found for orderId: {}", orderId);
                return null;
            });
    }
}
