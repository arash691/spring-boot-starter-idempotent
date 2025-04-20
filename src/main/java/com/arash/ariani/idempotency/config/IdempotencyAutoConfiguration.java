package com.arash.ariani.idempotency.config;


import com.arash.ariani.idempotency.context.IdempotencyHandler;
import com.arash.ariani.idempotency.context.aspect.IdempotencyAspect;
import com.arash.ariani.idempotency.context.executor.IdempotentExecutor;
import com.arash.ariani.idempotency.context.executor.IdempotentExecutors;
import com.arash.ariani.idempotency.scope.DefaultScopeResolver;
import com.arash.ariani.idempotency.scope.IdempotencyScopeResolver;
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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@EnableAspectJAutoProxy
@Configuration(enforceUniqueMethods = false)
@ConditionalOnClass(IdempotencyAspect.class)
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyAspect idempotencyAspect(ApplicationContext context, IdempotencyHandler handler) {
        return new IdempotencyAspect(context, handler);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyHandler idempotencyHandler(IdempotencyStore store, ApplicationContext context) {
        return new IdempotencyHandler(store, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentExecutor idempotentExecutor(IdempotencyHandler idempotencyHandler) {
        return new IdempotentExecutor(idempotencyHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyScopeResolver idempotencyScopeResolver() {
        return new DefaultScopeResolver();
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
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(factory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "idempotency.store", havingValue = "redis")
    public RedisIdempotencyStore redisIdempotencyStore(RedisTemplate<String, Object> redisTemplate) {
        return new RedisIdempotencyStore(redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentExecutors idempotentExecutors() {
        return new IdempotentExecutors();
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
