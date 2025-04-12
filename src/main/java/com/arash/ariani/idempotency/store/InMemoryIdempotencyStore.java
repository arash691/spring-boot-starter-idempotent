package com.arash.ariani.idempotency.store;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryIdempotencyStore implements IdempotencyStore {
    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    @Override
    public boolean exists(String key) {
        return store.containsKey(key) && !store.get(key).isExpired();
    }

    @Override
    public void save(String key, Object response, Duration ttl) {
        store.put(key, new CacheEntry(response, ttl));
    }

    @Override
    public Optional<Object> get(String key) {
        var entry = store.get(key);
        return entry != null && !entry.isExpired() ? Optional.of(entry.response) : Optional.empty();
    }

    record CacheEntry(Object response, Duration ttl, Instant createdAt) {
        CacheEntry(Object response, Duration ttl) {
            this(response, ttl, Instant.now());
        }
        boolean isExpired() {
            return Instant.now().isAfter(createdAt.plus(ttl));
        }
    }
}
