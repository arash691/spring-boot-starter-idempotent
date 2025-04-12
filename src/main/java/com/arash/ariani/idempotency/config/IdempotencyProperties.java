package com.arash.ariani.idempotency.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idempotency")
public class IdempotencyProperties {

    public enum StoreType { MEMORY, REDIS, JPA }

    private StoreType store = StoreType.MEMORY;

    private boolean initSchema = false;

    public boolean isInitSchema() {
        return initSchema;
    }

    public void setInitSchema(boolean initSchema) {
        this.initSchema = initSchema;
    }

    public StoreType getStore() {
        return store;
    }

    public void setStore(StoreType store) {
        this.store = store;
    }
}
