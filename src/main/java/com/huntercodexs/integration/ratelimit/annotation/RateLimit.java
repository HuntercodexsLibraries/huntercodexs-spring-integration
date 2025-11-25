package com.huntercodexs.integration.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

import static com.huntercodexs.integration.ratelimit.constants.IntegrationRateLimitConstants.RATE_LIMIT_DURATION_DEFAULT;
import static com.huntercodexs.integration.ratelimit.constants.IntegrationRateLimitConstants.RATE_LIMIT_LIMIT_DEFAULT;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    int limit() default RATE_LIMIT_LIMIT_DEFAULT;
    int duration() default RATE_LIMIT_DURATION_DEFAULT;
    TimeUnit unit() default TimeUnit.SECONDS;
}
