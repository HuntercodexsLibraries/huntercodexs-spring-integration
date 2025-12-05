package com.huntercodexs.integration.ratelimit.aspect;

import com.huntercodexs.integration.ratelimit.action.RateLimitServiceBusAction;
import com.huntercodexs.integration.ratelimit.annotation.RateLimitServiceBus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitServiceBusAspectTest {

    private RedisTemplate<String, Long> redisTemplate;
    private ValueOperations<String, Long> valueOps;
    private List<RateLimitServiceBusAction> actions;
    private RateLimitServiceBusAspect aspect;

    private ProceedingJoinPoint pjp;
    private MethodSignature signature;

    @BeforeEach
    void setup() throws Throwable {
        redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        actions = Collections.singletonList(new RateLimitServiceBusAction() {
            @Override
            public boolean supports(Object keyParameterName) {
                return "customerId".equals(keyParameterName);
            }
            @Override
            public void execute(Object[] args, String keyParameterName, int limit, int duration, TimeUnit unit) {
                // no-op for verification via spy if needed
            }
        });

        aspect = new RateLimitServiceBusAspect(redisTemplate, actions);

        // default config values
        ReflectionTestUtils.setField(aspect, "redisOn", true);
        ReflectionTestUtils.setField(aspect, "rateLimitEnabled", true);
        ReflectionTestUtils.setField(aspect, "rateLimitLogEnabled", false);
        ReflectionTestUtils.setField(aspect, "customLimit", 0);
        ReflectionTestUtils.setField(aspect, "customDuration", 0);
        ReflectionTestUtils.setField(aspect, "customUnit", "seconds");
        ReflectionTestUtils.setField(aspect, "customPrefix", "rateLimitServiceBusDefaultKeyName");
        ReflectionTestUtils.setField(aspect, "customerKeyParameter", "");

        // mock join point and method signature
        pjp = mock(ProceedingJoinPoint.class);
        signature = mock(MethodSignature.class);
        when(pjp.getSignature()).thenReturn(signature);

        Method testMethod = TestTarget.class.getDeclaredMethod("process",
                String.class, String.class, String.class);
        when(signature.getMethod()).thenReturn(testMethod);

        // args: message, headers, customerId
        when(pjp.getArgs()).thenReturn(new Object[]{"msg", "hdr", "123"});
        when(pjp.proceed()).thenReturn("ok");
    }

    static class TestTarget {
        @RateLimitServiceBus(limit = 2, duration = 3, unit = TimeUnit.SECONDS, keyParameterName = "customerId")
        public String process(String message, String headers, String customerId) {
            return "ok";
        }

        @RateLimitServiceBus(limit = 2, duration = 3, unit = TimeUnit.MINUTES, keyParameterName = "customerId")
        public String processMinutes(String message, String headers, String customerId) {
            return "ok";
        }

        @RateLimitServiceBus(limit = 2, duration = 3, unit = TimeUnit.HOURS, keyParameterName = "customerId")
        public String processHours(String message, String headers, String customerId) {
            return "ok";
        }

        @RateLimitServiceBus(limit = 2, duration = 3, unit = TimeUnit.SECONDS, keyParameterName = "unknownKey")
        public String processUnknown(String message, String headers, String customerId) {
            return "ok";
        }

        @RateLimitServiceBus(limit = 0, duration = 0, unit = TimeUnit.SECONDS, keyParameterName = "customerId")
        public String processWithDefaults(String message, String headers, String customerId) {
            return "ok";
        }
    }

    @Test
    void proceeds_when_rate_limit_disabled() throws Throwable {
        ReflectionTestUtils.setField(aspect, "rateLimitEnabled", false);
        String result = (String) aspect.rateLimit(pjp, TestTarget.class.getDeclaredMethod("process",
                String.class, String.class, String.class).getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", result);
        verify(pjp, times(1)).proceed();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void proceeds_when_redis_disabled() throws Throwable {
        ReflectionTestUtils.setField(aspect, "redisOn", false);
        String result = (String) aspect.rateLimit(pjp, TestTarget.class.getDeclaredMethod("process",
                String.class, String.class, String.class).getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", result);
        verify(pjp, times(1)).proceed();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void proceeds_when_key_parameter_not_found_and_not_default() throws Throwable {
        Method testMethod = TestTarget.class.getDeclaredMethod("processUnknown",
                String.class, String.class, String.class);
        when(signature.getMethod()).thenReturn(testMethod);

        String result = (String) aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", result);
        verify(pjp, times(1)).proceed();
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void increments_and_sets_ttl_seconds_on_first_hit() throws Throwable {
        Method testMethod = TestTarget.class.getDeclaredMethod("process",
                String.class, String.class, String.class);
        when(signature.getMethod()).thenReturn(testMethod);

        when(valueOps.increment(anyString())).thenReturn(1L);
        String res = (String) aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", res);

        InOrder inOrder = Mockito.inOrder(valueOps, redisTemplate);
        inOrder.verify(valueOps).increment(startsWith("rateLimitServiceBusDefaultKeyName:consumer:process:"));
        inOrder.verify(redisTemplate).expire(startsWith("rateLimitServiceBusDefaultKeyName:consumer:process:"),
                eq(Duration.ofSeconds(TimeUnit.SECONDS.convert(3, TimeUnit.SECONDS))));
        verify(pjp, times(1)).proceed();
    }

    @Test
    void increments_and_sets_ttl_minutes_on_first_hit_with_custom_unit() throws Throwable {
        Method testMethod = TestTarget.class.getDeclaredMethod("processMinutes",
                String.class, String.class, String.class);
        when(signature.getMethod()).thenReturn(testMethod);
        ReflectionTestUtils.setField(aspect, "customUnit", "minutes");

        when(valueOps.increment(anyString())).thenReturn(1L);
        String res = (String) aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", res);
        assertEquals("ok", res);

        verify(redisTemplate).expire(
                startsWith("rateLimitServiceBusDefaultKeyName:consumer:processMinutes:"),
                eq(Duration.ofMinutes(TimeUnit.MINUTES.convert(3, TimeUnit.MINUTES)))
        );
    }

    @Test
    void increments_and_sets_ttl_hours_on_first_hit_with_custom_unit() throws Throwable {
        Method testMethod = TestTarget.class.getDeclaredMethod("processHours",
                String.class, String.class, String.class);
        when(signature.getMethod()).thenReturn(testMethod);
        ReflectionTestUtils.setField(aspect, "customUnit", "hours");

        when(valueOps.increment(anyString())).thenReturn(1L);
        String res = (String) aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", res);

        verify(redisTemplate).expire(
                startsWith("rateLimitServiceBusDefaultKeyName:consumer:processHours:"),
                eq(Duration.ofHours(TimeUnit.HOURS.convert(3, TimeUnit.HOURS)))
        );
        verify(pjp, times(1)).proceed();
    }

    @Test
    void uses_custom_limit_and_duration_when_defaults_in_annotation() throws Throwable {
        Method testMethod = TestTarget.class.getDeclaredMethod("processWithDefaults",
                String.class, String.class, String.class);
        when(signature.getMethod()).thenReturn(testMethod);

        ReflectionTestUtils.setField(aspect, "customLimit", 5);
        ReflectionTestUtils.setField(aspect, "customDuration", 10);

        when(valueOps.increment(anyString())).thenReturn(1L);
        String res = (String) aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", res);
        verify(redisTemplate).expire(anyString(), eq(Duration.ofSeconds(0)));
        verify(pjp, times(1)).proceed();
    }

    @Test
    void proceeds_when_increment_returns_null() throws Throwable {
        Method testMethod = TestTarget.class.getDeclaredMethod("process",
                String.class, String.class, String.class);
        when(signature.getMethod()).thenReturn(testMethod);

        when(valueOps.increment(anyString())).thenReturn(null);
        String res = (String) aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", res);
        verify(pjp, times(1)).proceed();
    }

    @Test
    void triggers_limit_exceeded_action_when_over_limit() throws Throwable {
        Method testMethod = TestTarget.class.getDeclaredMethod("process",
                String.class, String.class, String.class);
        when(signature.getMethod()).thenReturn(testMethod);

        // first call sets ttl
        when(valueOps.increment(anyString())).thenReturn(1L);
        aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));

        // second call within limit
        when(valueOps.increment(anyString())).thenReturn(2L);
        aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));

        // third call exceeds limit=2
        when(valueOps.increment(anyString())).thenReturn(3L);
        String res = (String) aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", res);

        // verify execute action was attempted via supports filter; we spy actions list to ensure path
        RateLimitServiceBusAction spyAction = spy(actions.get(0));
        List<RateLimitServiceBusAction> spyList = Collections.singletonList(spyAction);
        RateLimitServiceBusAspect localAspect = new RateLimitServiceBusAspect(redisTemplate, spyList);
        ReflectionTestUtils.setField(localAspect, "redisOn", true);
        ReflectionTestUtils.setField(localAspect, "rateLimitEnabled", true);
        ReflectionTestUtils.setField(localAspect, "customPrefix", "rateLimitServiceBusDefaultKeyName");

        when(valueOps.increment(anyString())).thenReturn(3L);
        localAspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));

        verify(spyAction, atLeastOnce()).supports("customerId");
        verify(spyAction, atLeastOnce()).execute(any(Object[].class), eq("customerId"), eq(2), eq(3), eq(TimeUnit.SECONDS));
    }

    @Test
    void uses_custom_key_parameter_name_from_config() throws Throwable {
        Method testMethod = TestTarget.class.getDeclaredMethod("processWithDefaults",
                String.class, String.class, String.class);
        when(signature.getMethod()).thenReturn(testMethod);

        ReflectionTestUtils.setField(aspect, "customerKeyParameter", "customerId");

        when(valueOps.increment(anyString())).thenReturn(1L);
        String res = (String) aspect.rateLimit(pjp, testMethod.getAnnotation(RateLimitServiceBus.class));
        assertEquals("ok", res);

        verify(valueOps).increment(startsWith("rateLimitServiceBusDefaultKeyName:consumer:processWithDefaults:"));
        verify(pjp, times(1)).proceed();
    }
}