package com.arash.ariani.idempotency.store.redis;

import com.arash.ariani.idempotency.store.IdempotencyStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@ConditionalOnClass(RedisTemplate.class)
public class RedisIdempotencyStore implements IdempotencyStore {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisIdempotencyStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void save(String key, Object response, Duration ttl) {
        redisTemplate.opsForValue().set(key, response, ttl);
    }

    @Override
    public Optional<Object> get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value);
    }
}
