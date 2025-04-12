package com.arash.ariani.idempotency.context;

import com.arash.ariani.idempotency.annotation.Idempotent;
import org.aspectj.lang.ProceedingJoinPoint;

public interface IdempotencyContextProvider {
    String resolveKey(ProceedingJoinPoint pjp, Idempotent idempotent) throws Exception;
}
