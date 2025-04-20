package com.arash.ariani.idempotency.context.aspect;


import com.arash.ariani.idempotency.conflict.ConflictResolver;
import com.arash.ariani.idempotency.conflict.DefaultConflictResolver;
import com.arash.ariani.idempotency.scope.DefaultScopeResolver;
import com.arash.ariani.idempotency.scope.IdempotencyScopeResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String key() default "";

    String keyHeader() default "";

    String ttl() default "15m";

    Class<? extends IdempotencyScopeResolver> scopeResolver() default DefaultScopeResolver.class;

    Class<? extends ConflictResolver> conflictResolver() default DefaultConflictResolver.class;
}
