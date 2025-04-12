package com.arash.ariani.idempotency.store;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdempotencyStore implements IdempotencyStore {

    private record Entry(Object value, long expireAtMillis) {
    }

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public void save(String key, Object value, Duration ttl) {
        long expireAt = System.currentTimeMillis() + ttl.toMillis();
        store.put(key, new Entry(value, expireAt));
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(store.compute(key, (k, entry) -> {
            if (entry == null || isExpired(entry)) {
                return null;
            }
            return entry;
        })).map(Entry::value);
    }

    @Override
    public boolean exists(String key) {
        return get(key).isPresent();
    }

    private boolean isExpired(Entry entry) {
        return System.currentTimeMillis() > entry.expireAtMillis;
    }
}

