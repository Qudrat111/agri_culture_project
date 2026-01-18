package com.agri.common.idempotency;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Entity for idempotency key tracking.
 * Ensures duplicate requests with the same idempotency key return the same response.
 * Keys expire after 24 hours.
 */
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {
    
    @Id
    @Column(name = "key", length = 100)
    private String key;
    
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    protected IdempotencyKey() {
    }
    
    public IdempotencyKey(String key) {
        this.key = key;
        this.createdAt = Instant.now();
        this.expiresAt = createdAt.plus(24, ChronoUnit.HOURS);
    }
    
    public IdempotencyKey(String key, String response) {
        this(key);
        this.response = response;
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    
    // Getters and setters
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
