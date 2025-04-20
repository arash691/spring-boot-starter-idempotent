package com.arash.ariani.idempotency.conflict;

import java.text.MessageFormat;


public class DefaultConflictResolver implements ConflictResolver {
    @Override
    public <T> T resolve(String key, Object cached) {
        System.err.printf(MessageFormat.format("WARNING: Duplicated for key{0}", key));
        return null;
    }
}
