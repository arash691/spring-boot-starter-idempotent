package com.arash.ariani.idempotency.annotation;

import com.arash.ariani.idempotency.conflict.ConflictHandling;
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

    ConflictHandling onConflict() default ConflictHandling.THROW_409;

    Class<? extends IdempotencyScopeResolver> scopeResolver() default DefaultScopeResolver.class;

    boolean hashResponse() default false;

    int replayStatus() default 200;
}
