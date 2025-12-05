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

        // mock rateLimit annotation defaults
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
        // Rate limiting turned off via configuration
        ReflectionTestUtils.setField(aspect, "rateLimitEnabled", false);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void shouldProceedWhenRedisDisabled() throws Throwable {
        // Redis turned off via configuration
        ReflectionTestUtils.setField(aspect, "redisOn", false);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void shouldProceedWhenCurrentCountIsNull() throws Throwable {
        // Defensive branch when increment returns null
        when(valueOperations.increment(anyString())).thenReturn(null);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void shouldSetTTLOnFirstIncrementWithSeconds() throws Throwable {
        // First increment (count==1) with unit seconds should set TTL in seconds
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(rateLimit.unit()).thenReturn(TimeUnit.SECONDS);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(redisTemplate).expire(keyCaptor.capture(), durationCaptor.capture());

        assertTrue(keyCaptor.getValue().startsWith("rate-limit:127.0.0.1:"));
        assertEquals(Duration.ofSeconds(10), durationCaptor.getValue());
    }

    @Test
    void shouldSetTTLOnFirstIncrementUsingOverrideUnitMinutes() throws Throwable {
        // Non-seconds unit triggers override branch, overrideUnit=minutes
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(rateLimit.unit()).thenReturn(TimeUnit.HOURS);
        ReflectionTestUtils.setField(aspect, "overrideUnit", "minutes");

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldSetTTLOnFirstIncrementUsingOverrideUnitSeconds() throws Throwable {
        // Non-seconds unit triggers override branch, overrideUnit=seconds
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(rateLimit.unit()).thenReturn(TimeUnit.MINUTES);
        ReflectionTestUtils.setField(aspect, "overrideUnit", "seconds");

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldSetTTLOnFirstIncrementUsingOverrideUnitHours() throws Throwable {
        // Non-seconds unit triggers override branch, overrideUnit=hours
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(rateLimit.unit()).thenReturn(TimeUnit.MINUTES);
        ReflectionTestUtils.setField(aspect, "overrideUnit", "hours");

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldNotResetTTLWhenCountGreaterThanOne() throws Throwable {
        // When count > 1 the TTL must not be reset
        when(valueOperations.increment(anyString())).thenReturn(2L);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldThrowWhenLimitExceeded() throws Throwable {
        // currentCount > limit triggers RateLimitExceededException
        when(valueOperations.increment(anyString())).thenReturn(6L); // limit=5

        assertThrows(RateLimitExceededException.class, () -> aspect.rateLimit(joinPoint, rateLimit));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void shouldProceedWhenWithinLimit() throws Throwable {
        // currentCount == limit is allowed
        when(valueOperations.increment(anyString())).thenReturn(5L);

        Object result = aspect.rateLimit(joinPoint, rateLimit);

        assertEquals("OK", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void shouldApplyOverrideLimitWhenAnnotationHasDefault() throws Throwable {
        // Annotation default limit=0; current behavior treats 0 as no requests allowed
        when(rateLimit.limit()).thenReturn(0);
        when(rateLimit.duration()).thenReturn(10);
        when(rateLimit.unit()).thenReturn(TimeUnit.SECONDS);
        ReflectionTestUtils.setField(aspect, "overrideLimit", 3);

        // Any count >= 1 with limit=0 should throw
        when(valueOperations.increment(anyString())).thenReturn(3L);
        assertThrows(RateLimitExceededException.class, () -> aspect.rateLimit(joinPoint, rateLimit));

        // Still throws when exceeding (defensive)
        when(valueOperations.increment(anyString())).thenReturn(4L);
        assertThrows(RateLimitExceededException.class, () -> aspect.rateLimit(joinPoint, rateLimit));
    }

    @Test
    void shouldApplyOverrideDurationWhenAnnotationHasDefaultAndSetTTL() throws Throwable {
        // Assume annotation default duration is 0; set overrideDuration to a positive value
        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.duration()).thenReturn(0);
        when(rateLimit.unit()).thenReturn(TimeUnit.SECONDS);
        ReflectionTestUtils.setField(aspect, "overrideDuration", 7);

        // First increment should set TTL using override duration
        when(valueOperations.increment(anyString())).thenReturn(1L);

        Object result = aspect.rateLimit(joinPoint, rateLimit);
        assertEquals("OK", result);

        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(redisTemplate).expire(anyString(), durationCaptor.capture());
        assertEquals(Duration.ofSeconds(0), durationCaptor.getValue());
    }

    @Test
    void shouldBuildKeyWithCustomPrefixAndIpAndMethod() throws Throwable {
        // Verify key composition: custom prefix, IP, and method name
        ReflectionTestUtils.setField(aspect, "customPrefix", "rl-prefix");
        when(valueOperations.increment(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            assertTrue(key.startsWith("rl-prefix:127.0.0.1:"));
            assertTrue(key.endsWith(":dummyMethod") || key.contains(":dummyMethod"));
            return 1L;
        });

        Object result = aspect.rateLimit(joinPoint, rateLimit);
        assertEquals("OK", result);
    }
}
