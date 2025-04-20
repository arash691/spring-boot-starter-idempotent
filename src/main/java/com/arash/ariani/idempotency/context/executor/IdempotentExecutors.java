package com.arash.ariani.idempotency.context.executor;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;


public final class IdempotentExecutors implements ApplicationContextAware {

    private static ApplicationContext context;

    private static IdempotentExecutor getExecutor() {
        if (context == null) {
            throw new IllegalStateException("ApplicationContext not initialized yet.");
        }
        return context.getBean(IdempotentExecutor.class);
    }

    public static <T> T execute(String key, Duration ttl, Supplier<T> supplier) {
        return getExecutor().execute(key, ttl, supplier);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        IdempotentExecutors.context = applicationContext;
    }
}
