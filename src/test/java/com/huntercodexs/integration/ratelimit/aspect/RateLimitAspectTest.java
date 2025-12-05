package com.huntercodexs.integration.ratelimit.aspect;

import com.huntercodexs.integration.handler.exception.RateLimitExceededException;
import com.huntercodexs.integration.ratelimit.annotation.RateLimit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitAspectTest {

    private RedisTemplate<String, Long> redisTemplate;
    private ValueOperations<String, Long> valueOperations;
    private RateLimitAspect aspect;
    private ProceedingJoinPoint joinPoint;
    private MethodSignature methodSignature;
    private RateLimit rateLimit;

    private MockHttpServletRequest request;

    @BeforeEach
    void setup() throws Throwable {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        aspect = new RateLimitAspect(redisTemplate);

        // default config values
        ReflectionTestUtils.setField(aspect, "redisOn", true);
        ReflectionTestUtils.setField(aspect, "rateLimitEnabled", true);
        ReflectionTestUtils.setField(aspect, "overrideLimit", 0);
        ReflectionTestUtils.setField(aspect, "overrideDuration", 0);
        ReflectionTestUtils.setField(aspect, "overrideUnit", "minutes");
        ReflectionTestUtils.setField(aspect, "customPrefix", "rate-limit");

        // mock request context
        request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock join point and method
        joinPoint = mock(ProceedingJoinPoint.class);
        methodSignature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);

        Method dummyMethod = Dummy.class.getDeclaredMethod("dummyMethod");
        when(methodSignature.getMethod()).thenReturn(dummyMethod);

        // mock rateLimit annotation
        rateLimit = mock(RateLimit.class);
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.duration()).thenReturn(10);
        when(rateLimit.unit()).thenReturn(TimeUnit.SECONDS);

        when(joinPoint.proceed()).thenReturn("OK");
    }

    static class Dummy {
        public void dummyMethod() {}
    }

    @Test
    void shouldProceedWhenRateLimitDisabled() throws Throwable {
        ReflectionTestUtils.setField(aspect, "rateLimitEnabled", false);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void shouldProceedWhenRedisDisabled() throws Throwable {
        ReflectionTestUtils.setField(aspect, "redisOn", false);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void shouldProceedWhenCurrentCountIsNull() throws Throwable {
        when(valueOperations.increment(anyString())).thenReturn(null);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
    }

    @Test
    void shouldSetTTLOnFirstIncrementWithSeconds() throws Throwable {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(rateLimit.unit()).thenReturn(TimeUnit.SECONDS);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(redisTemplate).expire(keyCaptor.capture(), durationCaptor.capture());

        assertTrue(keyCaptor.getValue().startsWith("rate-limit:127.0.0.1:"));
        assertEquals(Duration.ofSeconds(TimeUnit.SECONDS.convert(10, TimeUnit.SECONDS)), durationCaptor.getValue());
    }

    @Test
    void shouldSetTTLOnFirstIncrementUsingOverrideUnitMinutes() throws Throwable {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(rateLimit.unit()).thenReturn(TimeUnit.HOURS); // non-seconds to enter override branch
        ReflectionTestUtils.setField(aspect, "overrideUnit", "minutes");

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldSetTTLOnFirstIncrementUsingOverrideUnitSeconds() throws Throwable {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(rateLimit.unit()).thenReturn(TimeUnit.MINUTES);
        ReflectionTestUtils.setField(aspect, "overrideUnit", "seconds");

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldSetTTLOnFirstIncrementUsingOverrideUnitHours() throws Throwable {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(rateLimit.unit()).thenReturn(TimeUnit.MINUTES);
        ReflectionTestUtils.setField(aspect, "overrideUnit", "hours");

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldNotResetTTLWhenCountGreaterThanOne() throws Throwable {
        when(valueOperations.increment(anyString())).thenReturn(2L);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldThrowWhenLimitExceeded() throws Throwable {
        when(valueOperations.increment(anyString())).thenReturn(6L); // currentCount > limit 5

        assertThrows(RateLimitExceededException.class, () -> aspect.rateLimit(joinPoint, rateLimit));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void shouldProceedWhenWithinLimit() throws Throwable {
        when(valueOperations.increment(anyString())).thenReturn(5L); // equal to limit

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(joinPoint, times(1)).proceed();
    }
}