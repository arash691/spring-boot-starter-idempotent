package com.arash.ariani.idempotency.context;

import com.arash.ariani.idempotency.conflict.ConflictResolver;

import java.time.Duration;

public record IdempotencyContext(
        String key,
        String keyHeader,
        Duration ttl,
        Class<? extends ConflictResolver> conflictResolver
) {
}
