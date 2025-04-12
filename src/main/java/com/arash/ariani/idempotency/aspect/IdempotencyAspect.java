package com.arash.ariani.idempotency.aspect;

import com.arash.ariani.idempotency.annotation.Idempotent;
import com.arash.ariani.idempotency.conflict.IdempotencyConflictResolver;
import com.arash.ariani.idempotency.scope.IdempotencyScopeResolver;
import com.arash.ariani.idempotency.store.IdempotencyStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
public class IdempotencyAspect {
    private final ExpressionParser parser = new SpelExpressionParser();
    private final MessageDigest digest = MessageDigest.getInstance("SHA-256");
    private final ApplicationContext context;
    private final IdempotencyStore idempotencyStore;
    private final ObjectMapper objectMapper;

    public IdempotencyAspect(ApplicationContext context, IdempotencyStore idempotencyStore, ObjectMapper objectMapper) throws NoSuchAlgorithmException {
        this.context = context;
        this.idempotencyStore = idempotencyStore;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        EvaluationContext evalContext = buildEvaluationContext(joinPoint);
        String key = resolveKey(idempotent, evalContext);
        Duration ttl = parseDuration(idempotent.ttl());

        String scope = resolveScope(idempotent.scopeResolver(), joinPoint);
        String fullKey = scope + ":" + key;

        if (idempotencyStore.exists(fullKey)) {
            Optional<Object> cached = idempotencyStore.get(fullKey);
            if (cached.isPresent()) {
                if (idempotent.hashResponse()) {
                    Object newResult = joinPoint.proceed();
                    if (!isSameResponse(cached.get(), newResult)) {
                        switch (idempotent.onConflict()) {
                            case THROW_409 ->
                                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflict: different result for same idempotency key");
                            case CUSTOM -> handleCustomConflict(fullKey, cached.get(), newResult);
                            case IGNORE -> {
                                return cached.get();
                            }
                        }
                    }
                }
                return cached.get();
            }
        }

        Object result = joinPoint.proceed();

        idempotencyStore.save(fullKey, result, ttl);
        return result;
    }

    private String resolveScope(Class<? extends IdempotencyScopeResolver> resolverClass, ProceedingJoinPoint joinPoint) {
        return context.getBean(resolverClass).resolveScope(joinPoint);
    }

    private Duration parseDuration(String raw) {
        try {
            if (raw.matches("^\\d+[smhd]$")) {
                long amount = Long.parseLong(raw.replaceAll("[^0-9]", ""));
                char unit = raw.charAt(raw.length() - 1);
                return switch (unit) {
                    case 's' -> Duration.ofSeconds(amount);
                    case 'm' -> Duration.ofMinutes(amount);
                    case 'h' -> Duration.ofHours(amount);
                    case 'd' -> Duration.ofDays(amount);
                    default -> Duration.ofMinutes(15);
                };
            } else {
                return Duration.parse("PT" + raw.toUpperCase());
            }
        } catch (Exception e) {
            return Duration.ofMinutes(15);
        }
    }


    private boolean isSameResponse(Object oldResp, Object newResp) {
        try {
            byte[] oldHash = digest.digest(objectMapper.writeValueAsBytes(oldResp));
            byte[] newHash = digest.digest(objectMapper.writeValueAsBytes(newResp));
            return Arrays.equals(oldHash, newHash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash responses", e);
        }
    }

    private void handleCustomConflict(String key, Object oldValue, Object newValue) {
        Map<String, IdempotencyConflictResolver> resolvers = context.getBeansOfType(IdempotencyConflictResolver.class);
        if (resolvers.isEmpty()) {
            throw new IllegalStateException("No ConflictResolver bean found for CUSTOM conflict handling");
        }
        for (IdempotencyConflictResolver resolver : resolvers.values()) {
            resolver.resolve(key, oldValue, newValue);
        }
    }


    private EvaluationContext buildEvaluationContext(ProceedingJoinPoint joinPoint) {
        EvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return context;
    }

    private String resolveKey(Idempotent idempotent, EvaluationContext context) {
        String rawKey = null;

        if (!idempotent.keyHeader().isBlank()) {
            HttpServletRequest request = getCurrentRequest();
            rawKey = request.getHeader(idempotent.keyHeader());
        } else if (!idempotent.key().isBlank()) {
            rawKey = parser.parseExpression(idempotent.key()).getValue(context, String.class);
        }

        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is missing or empty.");
        }

        return rawKey;
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("Cannot resolve HttpServletRequest");
        }
        return attrs.getRequest();
    }
}
