package com.agri.common.outbox;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Outbox pattern entity for reliable event publishing.
 * Events are stored in the same transaction as aggregate changes,
 * then published asynchronously by a scheduled job.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    
    @Id
    private String id;
    
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;
    
    @Column(name = "aggregate_id", nullable = false, length = 36)
    private String aggregateId;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "processed", nullable = false)
    private boolean processed = false;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
    
    protected OutboxEvent() {
    }
    
    public OutboxEvent(String id, String aggregateType, String aggregateId, 
                       String eventType, String payload) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.processed = false;
    }
    
    public void markAsProcessed() {
        this.processed = true;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAggregateType() {
        return aggregateType;
    }
    
    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getPayload() {
        return payload;
    }
    
    public void setPayload(String payload) {
        this.payload = payload;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isProcessed() {
        return processed;
    }
    
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
}
