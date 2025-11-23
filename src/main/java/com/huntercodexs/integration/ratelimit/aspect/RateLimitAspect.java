package com.huntercodexs.integration.ratelimit.aspect;

import com.huntercodexs.integration.handler.exception.RateLimitExceededException;
import com.huntercodexs.integration.ratelimit.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.huntercodexs.integration.constants.IntegrationConstants.*;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    @Value("${"+REDIS_APP_CONFIG+".enabled:true}")
    private boolean redisOn;

    @Value("${"+RATE_LIMIT_APP_CONFIG+".enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${"+RATE_LIMIT_APP_CONFIG+".limit:0}")
    private int overrideLimit;

    @Value("${"+RATE_LIMIT_APP_CONFIG+".duration:0}")
    private int overrideDuration;

    @Value("${"+RATE_LIMIT_APP_CONFIG+".unit:minutes}")
    private String overrideUnit;

    @Value("${"+RATE_LIMIT_APP_CONFIG+".cache-prefix:rate-limit}")
    private String customPrefix;

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RedisTemplate<String, Long> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        if (!rateLimitEnabled) {
            log.warn("Rate limiting is disabled via configuration.");
            return joinPoint.proceed();
        }

        if (!redisOn) {
            log.warn("Redis is disabled via configuration. Rate limiting cannot be applied.");
            return joinPoint.proceed();
        }

        // Data Request Information
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String ipAddress = request.getRemoteAddr();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Redis Key Construction
        // Format: ratelimit:<IP_CLIENT>:<METHOD_NAME>
        String redisKey = String.format(customPrefix+":%s:%s", ipAddress, method.getName());

        // Redis Operations
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            // Prevent null pointer exception, though it shouldn't happen
            return joinPoint.proceed();
        }

        // Rate Limit Parameters
        int limit = rateLimit.limit();
        if (overrideLimit > 0 && limit == LIMIT_RATE_LIMIT_DEFAULT) limit = overrideLimit;

        int duration = rateLimit.duration();
        if (overrideDuration > 0 && duration == DURATION_RATE_LIMIT_DEFAULT) duration = overrideDuration;

        // TTL Setup for the key on first increment
        TimeUnit unit = rateLimit.unit();

        if (!unit.equals(TimeUnit.SECONDS)) { // DEFAULT IS SECONDS

            if (overrideUnit.equalsIgnoreCase(TIME_UNIT_SECONDS)) {
                if (currentCount == 1) {
                    unit = TimeUnit.SECONDS;
                    redisTemplate.expire(redisKey, Duration.ofSeconds(TimeUnit.SECONDS.convert(duration, unit)));
                }
            } else if (overrideUnit.equalsIgnoreCase(TIME_UNIT_MINUTES)) {
                if (currentCount == 1) {
                    unit = TimeUnit.MINUTES;
                    redisTemplate.expire(redisKey, Duration.ofMinutes(TimeUnit.MINUTES.convert(duration, unit)));
                }
            } else if (overrideUnit.equalsIgnoreCase(TIME_UNIT_HOURS)) {
                if (currentCount == 1) {
                    unit = TimeUnit.HOURS;
                    redisTemplate.expire(redisKey, Duration.ofHours(TimeUnit.HOURS.convert(duration, unit)));
                }
            }
        } else {
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(TimeUnit.SECONDS.convert(duration, unit)));
            }
        }

        log.info("Rate Limit Check - key: {}, count: {}, limit: {}/{} {}", redisKey, currentCount, limit, duration, unit);

        // Limit Check
        if (currentCount > limit) {
            limitExceededAction(limit, duration, unit);
        }

        // Forward the request if within limit
        return joinPoint.proceed();
    }

    private void limitExceededAction(int limit, int duration, TimeUnit unit) {
        throw new RateLimitExceededException(String.format(MSG_RATE_LIMIT_EXCEEDED, limit, duration, unit.toString().toLowerCase()));
    }
}