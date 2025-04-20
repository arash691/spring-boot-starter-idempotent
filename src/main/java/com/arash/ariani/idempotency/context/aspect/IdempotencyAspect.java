package com.arash.ariani.idempotency.context.aspect;

import com.arash.ariani.idempotency.context.IdempotencyContext;
import com.arash.ariani.idempotency.context.IdempotencyHandler;
import com.arash.ariani.idempotency.scope.IdempotencyScopeResolver;
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
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.function.Supplier;

@Aspect
@Component
public final class IdempotencyAspect {
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ApplicationContext context;
    private final IdempotencyHandler handler;

    public IdempotencyAspect(ApplicationContext context, IdempotencyHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        EvaluationContext evalContext = buildEvaluationContext(joinPoint);
        String key = resolveKey(idempotent, evalContext);
        Duration ttl = parseDuration(idempotent.ttl());
        String scope = resolveScope(idempotent.scopeResolver());
        String fullKey = scope + ":" + key;


        IdempotencyContext ctx = new IdempotencyContext(fullKey, idempotent.keyHeader(), ttl, idempotent.conflictResolver());

        Supplier<Object> supplier = () -> {
            try {
                Object result = joinPoint.proceed();
                return isVoidMethod(joinPoint) ? IdempotencyHandler.VoidReturn.INSTANCE : result;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        };

        Object result = handler.execute(ctx, supplier);
        return isVoidMethod(joinPoint) ? null : result;
    }

    private boolean isVoidMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getReturnType().equals(Void.TYPE);
    }

    private String resolveScope(Class<? extends IdempotencyScopeResolver> resolverClass) {
        return context.getBean(resolverClass).resolveScope();
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
}
