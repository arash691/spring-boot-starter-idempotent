package com.arash.ariani.idempotency.store.jpa;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "idempotency_entries")
public class IdempotencyRecord {

    @Id
    private String key;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "ttl_expiry")
    private LocalDateTime ttlExpiry;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getTtlExpiry() {
        return ttlExpiry;
    }

    public void setTtlExpiry(LocalDateTime ttlExpiry) {
        this.ttlExpiry = ttlExpiry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdempotencyRecord that = (IdempotencyRecord) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
