package com.huntercodexs.integration.core.config;

import com.huntercodexs.integration.core.decoder.ErrorDecoderIntegration;
import com.huntercodexs.integration.core.interfaces.RetryInterceptorIntegration;
import com.huntercodexs.integration.core.retry.RetryLoggerIntegration;
import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientConfigIntegrationTest {

    private ClientConfigIntegration config;

    @Test
    void testClientConfigIntegrationLoads() {
        ClientConfigIntegration config = new ClientConfigIntegration();
        assertNotNull(config, "ClientConfigIntegration should be instantiated successfully");
    }

    @BeforeEach
    void setUp() {
        config = new ClientConfigIntegration();
        ReflectionTestUtils.setField(config, "period", 2000L);
        ReflectionTestUtils.setField(config, "maxPeriod", 3000L);
        ReflectionTestUtils.setField(config, "maxAttempts", 5);
        ReflectionTestUtils.setField(config, "logOn", true);
    }

    @Test
    void testDefaultErrorDecoder() {
        ErrorDecoder decoder = config.defaultErrorDecoder();
        assertNotNull(decoder);
        assertTrue(decoder instanceof ErrorDecoderIntegration);
    }

    @Test
    void testFeignLoggerLevel() {
        Logger.Level level = config.feignLoggerLevel();
        assertNotNull(level);
        assertEquals(Logger.Level.FULL, level);
    }

    @Test
    void testRetryer() {
        List<RetryInterceptorIntegration> interceptors = Collections.emptyList();
        Retryer retryer = config.retryer(interceptors);
        assertNotNull(retryer);
        assertTrue(retryer instanceof RetryLoggerIntegration);

        // Verifica se os campos foram passados corretamente
        assertEquals(2000L, ReflectionTestUtils.getField(retryer, "basePeriod"));
        assertEquals(3000L, ReflectionTestUtils.getField(retryer, "maxPeriod"));
        assertEquals(5, ReflectionTestUtils.getField(retryer, "maxAttempts"));
        assertEquals(true, ReflectionTestUtils.getField(retryer, "logOn"));
        assertEquals(interceptors, ReflectionTestUtils.getField(retryer, "interceptors"));
    }

}