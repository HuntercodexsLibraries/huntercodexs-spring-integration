package com.huntercodexs.integration.ratelimit.v2.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimitServiceBusV2 {
    int limit() default 5;
    int duration() default 10;
    TimeUnit unit() default TimeUnit.SECONDS;
    String keyParameterName() default "_MENSAGEM_INTEIRA_";
}
