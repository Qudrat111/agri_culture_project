package com.agri.inventory.application;

import com.agri.common.command.CompensateInventoryCommand;
import com.agri.common.command.ReserveInventoryCommand;
import com.agri.inventory.domain.InventoryItem;
import com.agri.inventory.domain.Reservation;
import com.agri.inventory.event.InventoryReservationFailedEvent;
import com.agri.inventory.event.InventoryReservedEvent;
import com.agri.inventory.infrastructure.kafka.EventPublisher;
import com.agri.inventory.infrastructure.repository.InventoryItemRepository;
import com.agri.inventory.infrastructure.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final InventoryItemRepository inventoryItemRepository;
    private final ReservationRepository reservationRepository;
    private final EventPublisher eventPublisher;
    
    /**
     * Reserve inventory for an order.
     * This is a transactional operation that checks availability, creates reservations,
     * and deducts from available quantity.
     */
    @Transactional
    public void reserveInventory(ReserveInventoryCommand command) {
        MDC.put("orderId", command.orderId());
        log.info("Processing inventory reservation for order: {}", command.orderId());
        
        try {
            // Check for idempotency - if reservation already exists, treat as success
            List<Reservation> existingReservations = reservationRepository.findByOrderId(command.orderId());
            if (!existingReservations.isEmpty()) {
                log.info("Reservation already exists for order: {}, treating as idempotent request", command.orderId());
                publishSuccessEvent(command.orderId(), existingReservations);
                return;
            }
            
            // Validate all items have sufficient inventory
            List<InventoryItem> itemsToReserve = new ArrayList<>();
            for (ReserveInventoryCommand.LineItem item : command.items()) {
                Optional<InventoryItem> inventoryItemOpt = 
                    inventoryItemRepository.findByProductIdWithLock(item.productId());
                
                if (inventoryItemOpt.isEmpty()) {
                    String reason = String.format("Product not found: %s", item.productId());
                    log.warn("Inventory reservation failed for order {}: {}", command.orderId(), reason);
                    eventPublisher.publishInventoryReservationFailedEvent(
                        new InventoryReservationFailedEvent(command.orderId(), reason)
                    );
                    return;
                }
                
                InventoryItem inventoryItem = inventoryItemOpt.get();
                if (!inventoryItem.hasAvailableQuantity(item.quantity())) {
                    String reason = String.format(
                        "Insufficient inventory for product %s (%s). Available: %d, Requested: %d",
                        inventoryItem.getProductId(),
                        inventoryItem.getProductName(),
                        inventoryItem.getAvailableQuantity(),
                        item.quantity()
                    );
                    log.warn("Inventory reservation failed for order {}: {}", command.orderId(), reason);
                    eventPublisher.publishInventoryReservationFailedEvent(
                        new InventoryReservationFailedEvent(command.orderId(), reason)
                    );
                    return;
                }
                
                itemsToReserve.add(inventoryItem);
            }
            
            // All items are available, proceed with reservation
            List<Reservation> reservations = new ArrayList<>();
            for (int i = 0; i < command.items().size(); i++) {
                ReserveInventoryCommand.LineItem item = command.items().get(i);
                InventoryItem inventoryItem = itemsToReserve.get(i);
                
                // Reserve inventory
                inventoryItem.reserve(item.quantity());
                inventoryItemRepository.save(inventoryItem);
                
                // Create reservation record
                Reservation reservation = new Reservation(
                    UUID.randomUUID().toString(),
                    command.orderId(),
                    item.productId(),
                    item.quantity()
                );
                reservations.add(reservationRepository.save(reservation));
                
                log.info("Reserved {} units of product {} for order {}", 
                    item.quantity(), item.productId(), command.orderId());
            }
            
            // Publish success event
            publishSuccessEvent(command.orderId(), reservations);
            log.info("Successfully reserved inventory for order: {}", command.orderId());
            
        } catch (Exception e) {
            log.error("Error processing inventory reservation for order {}: {}", 
                command.orderId(), e.getMessage(), e);
            eventPublisher.publishInventoryReservationFailedEvent(
                new InventoryReservationFailedEvent(
                    command.orderId(), 
                    "Error processing reservation: " + e.getMessage()
                )
            );
        } finally {
            MDC.remove("orderId");
        }
    }
    
    /**
     * Release inventory reservation for an order (SAGA compensation).
     * This adds the quantity back to available inventory and deletes the reservations.
     */
    @Transactional
    public void releaseInventory(CompensateInventoryCommand command) {
        MDC.put("orderId", command.orderId());
        log.info("Processing inventory release (compensation) for order: {}", command.orderId());
        
        try {
            List<Reservation> reservations = reservationRepository.findByOrderId(command.orderId());
            
            if (reservations.isEmpty()) {
                log.info("No reservations found for order: {}, nothing to compensate", command.orderId());
                return;
            }
            
            // Release each reservation
            for (Reservation reservation : reservations) {
                Optional<InventoryItem> inventoryItemOpt = 
                    inventoryItemRepository.findByProductIdWithLock(reservation.getProductId());
                
                if (inventoryItemOpt.isPresent()) {
                    InventoryItem inventoryItem = inventoryItemOpt.get();
                    inventoryItem.release(reservation.getQuantity());
                    inventoryItemRepository.save(inventoryItem);
                    
                    log.info("Released {} units of product {} for order {}", 
                        reservation.getQuantity(), reservation.getProductId(), command.orderId());
                } else {
                    log.warn("Inventory item not found for product: {}, skipping release", 
                        reservation.getProductId());
                }
            }
            
            // Delete reservations
            reservationRepository.deleteAll(reservations);
            log.info("Successfully released inventory for order: {} ({} items)", 
                command.orderId(), reservations.size());
            
        } catch (Exception e) {
            log.error("Error processing inventory release for order {}: {}", 
                command.orderId(), e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove("orderId");
        }
    }
    
    private void publishSuccessEvent(String orderId, List<Reservation> reservations) {
        List<InventoryReservedEvent.ReservedItem> reservedItems = reservations.stream()
            .map(r -> new InventoryReservedEvent.ReservedItem(r.getProductId(), r.getQuantity()))
            .toList();
        
        eventPublisher.publishInventoryReservedEvent(
            new InventoryReservedEvent(orderId, reservedItems)
        );
    }
}
