package com.huntercodexs.integration.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

import static com.huntercodexs.integration.constants.IntegrationConstants.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    int limit() default LIMIT_RATE_LIMIT_DEFAULT;
    int duration() default DURATION_RATE_LIMIT_DEFAULT;
    TimeUnit unit() default TimeUnit.SECONDS;
}
