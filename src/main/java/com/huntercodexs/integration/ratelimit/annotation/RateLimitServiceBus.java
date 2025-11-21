package com.huntercodexs.integration.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

import static com.huntercodexs.integration.constants.IntegrationConstants.KEY_PARAMETER_NAME_DEFAULT;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimitServiceBus {
    int limit() default 10;
    int duration() default 1;
    TimeUnit unit() default TimeUnit.SECONDS;
    String keyParameterName() default KEY_PARAMETER_NAME_DEFAULT;
}
