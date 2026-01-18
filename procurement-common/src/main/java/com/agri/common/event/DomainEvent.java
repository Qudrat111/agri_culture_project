package com.agri.common.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 * Follows Event Sourcing and Domain-Driven Design principles.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public abstract class DomainEvent {
    
    private String eventId;
    private Instant occurredOn;
    private String aggregateId;
    private Long version;
    
    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
    }
    
    protected DomainEvent(String aggregateId, Long version) {
        this();
        this.aggregateId = aggregateId;
        this.version = version;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public Instant getOccurredOn() {
        return occurredOn;
    }
    
    public void setOccurredOn(Instant occurredOn) {
        this.occurredOn = occurredOn;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public abstract String getEventType();
}
