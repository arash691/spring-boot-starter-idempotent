package com.arash.ariani.idempotency.store;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyStore {
    boolean exists(String key);

    void save(String key, Object response, Duration ttl);

    Optional<Object> get(String key);
}
