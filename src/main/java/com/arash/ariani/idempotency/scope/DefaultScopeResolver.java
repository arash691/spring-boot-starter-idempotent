package com.arash.ariani.idempotency.scope;

import org.aspectj.lang.ProceedingJoinPoint;

public class DefaultScopeResolver implements IdempotencyScopeResolver {
    @Override
    public String resolveScope(ProceedingJoinPoint joinPoint) {
        return "GLOBAL";
    }
}
