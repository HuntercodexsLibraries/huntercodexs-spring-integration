package com.huntercodexs.integration.ratelimit.action.impl;

import com.huntercodexs.integration.handler.exception.RateLimitExceededException;
import com.huntercodexs.integration.ratelimit.constants.RateLimitServiceBusIntegrationConstants;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceBusActionDefaultImplTest {

    private RateLimitServiceBusActionDefaultImpl newInstanceWithLogEnabled(boolean enabled) {
        RateLimitServiceBusActionDefaultImpl impl = new RateLimitServiceBusActionDefaultImpl();
        try {
            Field field = RateLimitServiceBusActionDefaultImpl.class.getDeclaredField("rateLimitLogEnabled");
            field.setAccessible(true);
            field.set(impl, enabled);
        } catch (Exception e) {
            fail("Falha ao configurar o campo 'rateLimitLogEnabled' via reflexão: " + e.getMessage());
        }
        return impl;
    }

    @Test
    void supportsDeveRetornarTrueParaChaveDefault() {
        RateLimitServiceBusActionDefaultImpl impl = new RateLimitServiceBusActionDefaultImpl();
        String defaultKey = RateLimitServiceBusIntegrationConstants.RATE_LIMIT_SERVICE_BUS_KEY_PARAMETER_NAME_DEFAULT;
        assertTrue(impl.supports(defaultKey));
    }

    @Test
    void supportsDeveRetornarFalseParaOutraChave() {
        RateLimitServiceBusActionDefaultImpl impl = new RateLimitServiceBusActionDefaultImpl();
        assertFalse(impl.supports("OUTRA_CHAVE"));
    }

    @Test
    void executeDeveLancarRateLimitExceededExceptionComLogAtivo() {
        RateLimitServiceBusActionDefaultImpl impl = newInstanceWithLogEnabled(true);
        RateLimitExceededException ex = assertThrows(
                RateLimitExceededException.class,
                () -> impl.execute(new Object[]{}, "TEST_KEY", 5, 10, TimeUnit.SECONDS)
        );
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    @Test
    void executeDeveLancarRateLimitExceededExceptionComLogDesativado() {
        RateLimitServiceBusActionDefaultImpl impl = newInstanceWithLogEnabled(false);
        RateLimitExceededException ex = assertThrows(
                RateLimitExceededException.class,
                () -> impl.execute(new Object[]{}, "TEST_KEY", 3, 1, TimeUnit.MINUTES)
        );
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }
}