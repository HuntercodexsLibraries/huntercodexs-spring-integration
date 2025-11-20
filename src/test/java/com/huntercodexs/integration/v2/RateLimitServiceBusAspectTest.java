package com.huntercodexs.integration.v2;

import com.huntercodexs.integration.ratelimit.v2.annotation.RateLimitServiceBusV2;
import com.huntercodexs.integration.ratelimit.v2.aspect.RateLimitServiceBusAspectV2;
import com.huntercodexs.integration.ratelimit.v2.handler.exception.RateLimitExceededExceptionV2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceBusAspectTest {

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private ValueOperations<String, Long> valueOperations;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private RateLimitServiceBusV2 rateLimitServiceBus;

    @InjectMocks
    private RateLimitServiceBusAspectV2 rateLimitServiceBusAspect;

    private static final String KEY_PARAM_NAME = "message";
    private static final String EXPECTED_KEY_PREFIX = "rateLimitServiceBusDefaultKeyName:consumer:processMessage:" + KEY_PARAM_NAME;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "rateLimitEnabled", true);
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "customLimit", 0);
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "customDuration", 0);
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "customUnit", "SECONDS");
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "customPrefix", "rateLimitServiceBusDefaultKeyName");
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "customerKeyParameter", "");
    }

    @Test
    void should_allowRequest_when_underLimit() throws Throwable {
        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(1L);

        rateLimitServiceBusAspect.rateLimit(joinPoint, rateLimitServiceBus);

        verify(valueOperations, times(1)).increment(eq(EXPECTED_KEY_PREFIX));
        verify(redisTemplate, times(1)).expire(eq(EXPECTED_KEY_PREFIX), eq(Duration.ofSeconds(10)));
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void should_throwException_when_limitExceeded() throws Throwable {
        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(3L);

        assertThrows(RateLimitExceededExceptionV2.class,
                () -> rateLimitServiceBusAspect.rateLimit(joinPoint, rateLimitServiceBus));

        verify(joinPoint, never()).proceed();
    }

    @Test
    void should_notSetExpiration_onSubsequentCalls() throws Throwable {
        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(2L);

        rateLimitServiceBusAspect.rateLimit(joinPoint, rateLimitServiceBus);

        verify(valueOperations, times(1)).increment(eq(EXPECTED_KEY_PREFIX));
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void should_useCustomLimit_when_customLimitIsSet() throws Throwable {
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "customLimit", 1);

        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(2L);

        assertThrows(RateLimitExceededExceptionV2.class,
                () -> rateLimitServiceBusAspect.rateLimit(joinPoint, rateLimitServiceBus));

        verify(valueOperations, times(1)).increment(anyString());
        verify(joinPoint, never()).proceed();
    }

    @Test
    void should_useCustomDurationAndUnit_when_customUnitIsMINUTES() throws Throwable {
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "customDuration", 5);
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "customUnit", "MINUTES");

        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(1L);

        rateLimitServiceBusAspect.rateLimit(joinPoint, rateLimitServiceBus);

        verify(redisTemplate, times(1)).expire(eq(EXPECTED_KEY_PREFIX), eq(Duration.ofMinutes(5)));
    }

    @Test
    void should_useCustomPrefix_when_customPrefixIsSet() throws Throwable {
        String customPrefix = "CustomRedisPrefix";
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "customPrefix", customPrefix);

        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimitServiceBusAspect.rateLimit(joinPoint, rateLimitServiceBus);

        String expectedKey = customPrefix + ":consumer:processMessage:" + KEY_PARAM_NAME;
        verify(valueOperations, times(1)).increment(eq(expectedKey));
    }

    @Test
    void should_disableRateLimit_when_rateLimitEnabledIsFalse() throws Throwable {
        ReflectionTestUtils.setField(rateLimitServiceBusAspect, "rateLimitEnabled", false);

        rateLimitServiceBusAspect.rateLimit(joinPoint, rateLimitServiceBus);

        verify(valueOperations, never()).increment(anyString());
        verify(joinPoint, times(1)).proceed();
    }
}