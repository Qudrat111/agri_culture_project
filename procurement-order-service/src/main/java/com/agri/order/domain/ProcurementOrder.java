package com.agri.order.domain;

import com.agri.common.event.DomainEvent;
import com.agri.order.domain.event.OrderCancelledEvent;
import com.agri.order.domain.event.OrderCompletedEvent;
import com.agri.order.domain.event.OrderConfirmedEvent;
import com.agri.order.domain.event.OrderCreatedEvent;
import com.agri.order.domain.vo.BuyerId;
import com.agri.order.domain.vo.OrderItem;
import com.agri.order.domain.vo.SupplierId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "procurement_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcurementOrder {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "buyer_id", nullable = false, length = 36)
    private String buyerId;
    
    @Column(name = "supplier_id", nullable = false, length = 36)
    private String supplierId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
    
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    public ProcurementOrder(String id, BuyerId buyerId, SupplierId supplierId, List<OrderItem> items) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (buyerId == null) {
            throw new IllegalArgumentException("BuyerId cannot be null");
        }
        if (supplierId == null) {
            throw new IllegalArgumentException("SupplierId cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        
        this.id = id;
        this.buyerId = buyerId.value();
        this.supplierId = supplierId.value();
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
        this.totalAmount = calculateTotalAmount();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        
        registerEvent(new OrderCreatedEvent(
            this.id,
            this.version,
            this.buyerId,
            this.supplierId,
            new ArrayList<>(this.items),
            this.totalAmount
        ));
    }
    
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                String.format("Cannot confirm order in status %s. Only PENDING orders can be confirmed.", status)
            );
        }
        
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
        
        registerEvent(new OrderConfirmedEvent(this.id, this.version));
    }
    
    public void cancel(String reason) {
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed order");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
        
        registerEvent(new OrderCancelledEvent(this.id, this.version, reason));
    }
    
    public void complete() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                String.format("Cannot complete order in status %s. Only CONFIRMED orders can be completed.", status)
            );
        }
        
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = Instant.now();
        
        registerEvent(new OrderCompletedEvent(this.id, this.version));
    }
    
    public BuyerId getBuyerIdVO() {
        return new BuyerId(this.buyerId);
    }
    
    public SupplierId getSupplierIdVO() {
        return new SupplierId(this.supplierId);
    }
    
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    private BigDecimal calculateTotalAmount() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
}
