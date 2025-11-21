package com.huntercodexs.integration.ratelimit.action;

import java.util.concurrent.TimeUnit;

public interface RateLimitAction {

    boolean supports(Object value);
    void execute(Object[] args, String keyName, int limit, int duration, TimeUnit timeUnit);

}
