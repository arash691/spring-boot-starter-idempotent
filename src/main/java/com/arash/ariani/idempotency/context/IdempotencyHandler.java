package com.arash.ariani.idempotency.context;

import com.arash.ariani.idempotency.conflict.DefaultConflictResolver;
import com.arash.ariani.idempotency.store.IdempotencyStore;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public final class IdempotencyHandler {
    private final IdempotencyStore store;
    private final ApplicationContext context;

    public IdempotencyHandler(IdempotencyStore store, ApplicationContext context) {
        this.store = store;
        this.context = context;
    }

    public <T> T execute(IdempotencyContext ctx, Supplier<T> supplier) {
        String key = ctx.key();

        if (store.exists(key)) {
            Object res = store.get(key).orElse(null);
            if (ctx.conflictResolver().isInstance(DefaultConflictResolver.class)) {
                context.getBean(ctx.conflictResolver()).resolve(key, res);
                return null;
            }
            return context.getBean(ctx.conflictResolver()).resolve(key, res);
        }

        T result = supplier.get();
        store.save(key, wrap(result), ctx.ttl());
        return result;
    }

    private Object wrap(Object result) {
        if (result == null) return NullReturn.INSTANCE;
        return result;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public static final class VoidReturn implements java.io.Serializable {
        public static final VoidReturn INSTANCE = new VoidReturn();
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public static final class NullReturn implements java.io.Serializable {
        public static final NullReturn INSTANCE = new NullReturn();
    }
}
