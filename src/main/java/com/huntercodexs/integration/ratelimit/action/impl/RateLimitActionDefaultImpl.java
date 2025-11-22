package com.huntercodexs.integration.ratelimit.action.impl;

import com.huntercodexs.integration.handler.exception.RateLimitExceededException;
import com.huntercodexs.integration.ratelimit.action.RateLimitAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.huntercodexs.integration.constants.IntegrationConstants.*;

@Component
public class RateLimitActionDefaultImpl implements RateLimitAction {

    private static final Logger log = LoggerFactory.getLogger(RateLimitActionDefaultImpl.class);

    @Value("${"+ RATE_LIMIT_SERVICE_BUS_LOG_APP_CONFIG +".enabled:false}")
    private boolean rateLimitLogEnabled;

    @Override
    public boolean supports(Object value) {
        return value.toString().equals(KEY_PARAMETER_NAME_DEFAULT);
    }

    @Override
    public void execute(Object[] args, String keyName, int limit, int duration, TimeUnit timeUnit) {

        if (rateLimitLogEnabled) {
            log.error("No RateLimitAction strategy found for key parameter: {}. Throwing exception by default.", keyName);
        }

        throw new RateLimitExceededException(String.format(
                MSG_RATE_LIMIT_SERVICE_BUS_EXCEEDED, limit, keyName, duration, timeUnit.toString().toLowerCase()));

    }
}
