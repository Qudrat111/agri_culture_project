package com.agri.saga.infrastructure.kafka;

import com.agri.common.command.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String INVENTORY_COMMANDS_TOPIC = "inventory.commands";
    private static final String PAYMENT_COMMANDS_TOPIC = "payment.commands";
    private static final String ORDER_COMMANDS_TOPIC = "order.commands";
    
    public CommandPublisher(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void publishReserveInventoryCommand(ReserveInventoryCommand command) {
        log.info("Publishing ReserveInventoryCommand for orderId: {}", command.orderId());
        kafkaTemplate.send(INVENTORY_COMMANDS_TOPIC, command.orderId(), command)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish ReserveInventoryCommand for orderId: {}", command.orderId(), ex);
                } else {
                    log.debug("ReserveInventoryCommand published successfully for orderId: {}", command.orderId());
                }
            });
    }
    
    public void publishProcessPaymentCommand(ProcessPaymentCommand command) {
        log.info("Publishing ProcessPaymentCommand for orderId: {}", command.orderId());
        kafkaTemplate.send(PAYMENT_COMMANDS_TOPIC, command.orderId(), command)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish ProcessPaymentCommand for orderId: {}", command.orderId(), ex);
                } else {
                    log.debug("ProcessPaymentCommand published successfully for orderId: {}", command.orderId());
                }
            });
    }
    
    public void publishConfirmOrderCommand(ConfirmOrderCommand command) {
        log.info("Publishing ConfirmOrderCommand for orderId: {}", command.orderId());
        kafkaTemplate.send(ORDER_COMMANDS_TOPIC, command.orderId(), command)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish ConfirmOrderCommand for orderId: {}", command.orderId(), ex);
                } else {
                    log.debug("ConfirmOrderCommand published successfully for orderId: {}", command.orderId());
                }
            });
    }
    
    public void publishCompensateInventoryCommand(CompensateInventoryCommand command) {
        log.info("Publishing CompensateInventoryCommand for orderId: {}", command.orderId());
        kafkaTemplate.send(INVENTORY_COMMANDS_TOPIC, command.orderId(), command)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish CompensateInventoryCommand for orderId: {}", command.orderId(), ex);
                } else {
                    log.debug("CompensateInventoryCommand published successfully for orderId: {}", command.orderId());
                }
            });
    }
}
