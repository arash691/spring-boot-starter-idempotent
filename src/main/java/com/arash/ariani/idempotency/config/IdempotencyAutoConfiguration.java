package com.arash.ariani.idempotency.config;

import com.arash.ariani.idempotency.aspect.IdempotencyAspect;
import com.arash.ariani.idempotency.store.IdempotencyStore;
import com.arash.ariani.idempotency.store.InMemoryIdempotencyStore;
import com.arash.ariani.idempotency.store.jpa.IdempotencyRecord;
import com.arash.ariani.idempotency.store.jpa.IdempotencyRecordRepository;
import com.arash.ariani.idempotency.store.jpa.JpaIdempotencyStore;
import com.arash.ariani.idempotency.store.redis.RedisIdempotencyStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.security.NoSuchAlgorithmException;

@EnableAspectJAutoProxy
@Configuration(enforceUniqueMethods = false)
@ConditionalOnClass(IdempotencyAspect.class)
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyAspect idempotencyAspect(
            ApplicationContext context,
            IdempotencyStore idempotencyStore,
            ObjectMapper objectMapper
    ) throws NoSuchAlgorithmException {
        return new IdempotencyAspect(context, idempotencyStore, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    @ConditionalOnProperty(name = "idempotency.store", havingValue = "memory", matchIfMissing = true)
    public InMemoryIdempotencyStore inMemoryIdempotencyStore() {
        return new InMemoryIdempotencyStore();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "idempotency.store", havingValue = "redis")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "idempotency.store", havingValue = "redis")
    public RedisIdempotencyStore redisIdempotencyStore(RedisTemplate<String, Object> redisTemplate) {
        return new RedisIdempotencyStore(redisTemplate);
    }

    @Configuration
    @ConditionalOnProperty(name = "idempotency.store", havingValue = "jpa")
    @EnableJpaRepositories(basePackageClasses = IdempotencyRecordRepository.class)
    @EntityScan(basePackageClasses = IdempotencyRecord.class)
    static class JpaIdempotencyConfiguration {

        @Bean
        @ConditionalOnMissingBean(IdempotencyStore.class)
        public IdempotencyStore jpaIdempotencyStore(IdempotencyRecordRepository repo, ObjectMapper objectMapper) {
            return new JpaIdempotencyStore(repo, objectMapper);
        }
    }
}

