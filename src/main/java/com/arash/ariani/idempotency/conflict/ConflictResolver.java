package com.arash.ariani.idempotency.conflict;

public interface ConflictResolver {
    <T> T resolve(String key, Object cached);
}
