package com.arash.ariani.idempotency.context.executor;

import com.arash.ariani.idempotency.conflict.DefaultConflictResolver;
import com.arash.ariani.idempotency.context.IdempotencyContext;
import com.arash.ariani.idempotency.context.IdempotencyHandler;

import java.time.Duration;
import java.util.function.Supplier;


public class IdempotentExecutor {
    private final static String SCOPE = "GLOBAL";
    private final static Duration DEFAULT_TTL = Duration.ofMinutes(15);

    private final IdempotencyHandler handler;

    public IdempotentExecutor(IdempotencyHandler handler) {
        this.handler = handler;
    }

    public <T> T execute(String key, Duration ttl, Supplier<T> supplier) {
        IdempotencyContext ctx = new IdempotencyContext(SCOPE + ":" + key, null, ttl, DefaultConflictResolver.class);
        return handler.execute(ctx, supplier);
    }
}
