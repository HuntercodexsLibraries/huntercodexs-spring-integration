package com.huntercodexs.integration.ratelimit.aspect;

import com.huntercodexs.integration.ratelimit.action.RateLimitAction;
import com.huntercodexs.integration.ratelimit.annotation.RateLimitServiceBus;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.huntercodexs.integration.constants.IntegrationConstants.*;

@Aspect
@Component
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S3457"})
public class RateLimitServiceBusAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitServiceBusAspect.class);

    @Value("${huntercodexs.integration.rate-limit-service-bus.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${huntercodexs.integration.rate-limit-service-bus.log.enabled:false}")
    private boolean rateLimitLogEnabled;

    @Value("${huntercodexs.integration.redis.enabled:true}")
    private boolean redisOn;

    @Value("${huntercodexs.integration.rate-limit-service-bus.limit:0}")
    private int customLimit;

    @Value("${huntercodexs.integration.rate-limit-service-bus.duration:0}")
    private int customDuration;

    @Value("${huntercodexs.integration.rate-limit-service-bus.unit:seconds}")
    private String customUnit;

    @Value("${huntercodexs.integration.rate-limit-service-bus.cache-prefix:rateLimitServiceBusDefaultKeyName}")
    private String customPrefix;

    @Value("${huntercodexs.integration.rate-limit-service-bus.key-parameter:}")
    private String customerKeyParameter;

    private final RedisTemplate<String, Long> redisTemplate;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();
    private final List<RateLimitAction> actions;

    @Around("@annotation(rateLimitServiceBus)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimitServiceBus rateLimitServiceBus) throws Throwable {

        if (!rateLimitEnabled) {
            log.warn("Rate limiting service bus is disabled via configuration.");
            return joinPoint.proceed();
        }

        if (!redisOn) {
            log.warn("Redis is disabled via configuration. Rate limiting cannot be applied.");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs(); // Get method arguments (message, headers, etc.)

        // Getting Rate Limit Key Value
        String keyParameterName = rateLimitServiceBus.keyParameterName();
        if (customerKeyParameter != null && !customerKeyParameter.isEmpty()) keyParameterName = customerKeyParameter;

        Object rateLimitKeyValue = findParameterValue(method, args, keyParameterName);

        if (rateLimitKeyValue == null && !keyParameterName.equals(KEY_PARAMETER_NAME_DEFAULT)) {
            // If the key parameter is not found or is null, handle accordingly.
            log.warn("Alert: Rate Limit key parameter not found or is null. Request allowed.");
            return joinPoint.proceed();
        }

        // Building the Redis Key - Format: rateLimitServiceBusKeyName:consumer:<METHOD_NAME>:<KEY_VALUE>
        String redisKey = String.format(customPrefix + ":consumer:%s:%s", method.getName(), keyParameterName);

        // Rate limiting logic
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            // Prevent null pointer exception, though it shouldn't happen
            return joinPoint.proceed();
        }

        // Get values from annotation
        int limit = rateLimitServiceBus.limit();
        if (customLimit > 0) limit = customLimit;

        int duration = rateLimitServiceBus.duration();
        if (customDuration > 0) duration = customDuration;

        // TTL Setup for the key on first increment
        TimeUnit unit = rateLimitServiceBus.unit();

        if (customUnit.equalsIgnoreCase(TIME_UNIT_SECONDS)) {
            if (currentCount == 1) {
                unit = TimeUnit.SECONDS;
                redisTemplate.expire(redisKey, Duration.ofSeconds(TimeUnit.SECONDS.convert(duration, unit)));
            }
        } else if (customUnit.equalsIgnoreCase(TIME_UNIT_MINUTES)) {
            if (currentCount == 1) {
                unit = TimeUnit.MINUTES;
                redisTemplate.expire(redisKey, Duration.ofMinutes(TimeUnit.MINUTES.convert(duration, unit)));
            }
        } else if (customUnit.equalsIgnoreCase(TIME_UNIT_HOURS)) {
            if (currentCount == 1) {
                unit = TimeUnit.HOURS;
                redisTemplate.expire(redisKey, Duration.ofHours(TimeUnit.HOURS.convert(duration, unit)));
            }
        } else {
            if (currentCount == 1) {
                unit = TimeUnit.SECONDS;
                redisTemplate.expire(redisKey, Duration.ofSeconds(TimeUnit.SECONDS.convert(duration, unit)));
            }
        }

        if (rateLimitLogEnabled) log.info("Rate Limit Service Bus Check - key: {}, count: {}, limit: {}/{} {}", redisKey, currentCount, limit, duration, unit);

        if (currentCount > limit) {
            limitExceededAction(args, keyParameterName, limit, duration, unit);
        }

        // Proceed with the method execution
        return joinPoint.proceed();
    }

    private void limitExceededAction(Object[] args, Object keyParameterName, int limit, int duration, TimeUnit unit) {

        String exceededMessage = String.format(MSG_RATE_LIMIT_SERVICE_BUS_EXCEEDED_2, keyParameterName, limit, duration, unit.toString().toLowerCase());

        if (rateLimitLogEnabled) log.error("429 TOO_MANY_REQUESTS - {}", exceededMessage);

        actions.stream()
                .filter(action -> action.supports(keyParameterName))
                .findFirst()
                .ifPresent(strategy -> strategy.execute(args, keyParameterName.toString(), limit, duration, unit));

    }

    private Object findParameterValue(Method method, Object[] args, String parameterName) {
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(parameterName)) {
                    return args[i];
                }
            }
        }
        return null;
    }
}