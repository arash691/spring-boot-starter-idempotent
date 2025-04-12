package com.arash.ariani.idempotency.scope;

import org.aspectj.lang.ProceedingJoinPoint;

public interface IdempotencyScopeResolver {
    String resolveScope(ProceedingJoinPoint joinPoint);
}
