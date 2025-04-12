package com.arash.ariani.idempotency.conflict;

public enum ConflictHandling {
    IGNORE,        // Always return stored response, don't check payload
    THROW_409,     // Throw 409 if payload hash differs
    CUSTOM         // Delegate to custom ConflictResolver bean
}
