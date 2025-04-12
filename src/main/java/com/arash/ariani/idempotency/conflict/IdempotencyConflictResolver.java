package com.arash.ariani.idempotency.conflict;

public interface IdempotencyConflictResolver {
    void resolve(String key, Object existingResponse, Object newResponse);
}
