package com.agri.inventory.infrastructure.kafka;

import com.agri.common.command.CompensateInventoryCommand;
import com.agri.common.command.ReserveInventoryCommand;
import com.agri.inventory.application.InventoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandListener {
    
    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;
    
    /**
     * Listen to inventory commands from the SAGA orchestrator.
     * Uses retry mechanism with exponential backoff for resilience.
     */
    @KafkaListener(
        topics = "inventory.commands",
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleCommand(String message) {
        try {
            log.debug("Received command: {}", message);
            
            // Parse message to determine command type
            JsonNode rootNode = objectMapper.readTree(message);
            String commandType = rootNode.has("@type") 
                ? rootNode.get("@type").asText() 
                : determineCommandType(rootNode);
            
            if (commandType.contains("ReserveInventoryCommand")) {
                ReserveInventoryCommand command = objectMapper.readValue(message, ReserveInventoryCommand.class);
                MDC.put("commandType", "ReserveInventory");
                MDC.put("orderId", command.orderId());
                log.info("Processing ReserveInventoryCommand for order: {}", command.orderId());
                inventoryService.reserveInventory(command);
            } else if (commandType.contains("CompensateInventoryCommand")) {
                CompensateInventoryCommand command = objectMapper.readValue(message, CompensateInventoryCommand.class);
                MDC.put("commandType", "CompensateInventory");
                MDC.put("orderId", command.orderId());
                log.info("Processing CompensateInventoryCommand for order: {}", command.orderId());
                inventoryService.releaseInventory(command);
            } else {
                log.warn("Unknown command type: {}", commandType);
            }
            
        } catch (Exception e) {
            log.error("Error processing command: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process command", e);
        } finally {
            MDC.clear();
        }
    }
    
    private String determineCommandType(JsonNode rootNode) {
        // Fallback: determine by presence of fields
        if (rootNode.has("items")) {
            return "ReserveInventoryCommand";
        } else if (rootNode.has("orderId") && !rootNode.has("items")) {
            return "CompensateInventoryCommand";
        }
        return "Unknown";
    }
}
