package com.arash.ariani.idempotency.store.redis;

import com.arash.ariani.idempotency.store.IdempotencyStore;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;

public class RedisIdempotencyStore implements IdempotencyStore {

    private final static String PREFIX = "idem:";
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisIdempotencyStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + key));
    }

    @Override
    public void save(String key, Object response, Duration ttl) {
        redisTemplate.opsForValue().set(PREFIX + key, response, ttl);
    }

    @Override
    public Optional<Object> get(String key) {
        Object value = redisTemplate.opsForValue().get(PREFIX + key);
        return Optional.ofNullable(value);
    }
}
