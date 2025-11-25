package com.huntercodexs.integration.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

import static com.huntercodexs.integration.ratelimit.constants.IntegrationRateLimitServiceBusConstants.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimitServiceBus {
    int limit() default RATE_LIMIT_SERVICE_BUS_LIMIT_DEFAULT;
    int duration() default RATE_LIMIT_SERVICE_BUS_DURATION_DEFAULT;
    TimeUnit unit() default TimeUnit.SECONDS;
    String keyParameterName() default RATE_LIMIT_SERVICE_BUS_KEY_PARAMETER_NAME_DEFAULT;
}
