package com.huntercodexs.integration.ratelimit.action.impl;

import com.huntercodexs.integration.handler.exception.RateLimitExceededException;
import com.huntercodexs.integration.ratelimit.action.RateLimitServiceBusAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.huntercodexs.integration.ratelimit.constants.RateLimitServiceBusIntegrationConstants.*;

@Component
public class RateLimitServiceBusActionDefaultImpl implements RateLimitServiceBusAction {

    private static final Logger log = LoggerFactory.getLogger(RateLimitServiceBusActionDefaultImpl.class);

    @Value("${"+ RATE_LIMIT_SERVICE_BUS_LOG_APP_CONFIG +".enabled:false}")
    private boolean rateLimitLogEnabled;

    @Override
    public boolean supports(Object value) {
        return value.toString().equals(RATE_LIMIT_SERVICE_BUS_KEY_PARAMETER_NAME_DEFAULT);
    }

    @Override
    public void execute(Object[] args, String keyName, int limit, int duration, TimeUnit timeUnit) {

        if (rateLimitLogEnabled) {
            log.warn("No specific RateLimitServiceBusAction strategy found, using the key parameter __DEFAULT__");
        }

        throw new RateLimitExceededException(String.format(
                RATE_LIMIT_SERVICE_BUS_MSG_EXCEEDED, limit, keyName, duration, timeUnit.toString().toLowerCase()));

    }
}
