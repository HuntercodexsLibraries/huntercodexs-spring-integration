package com.huntercodexs.integration.ratelimit.v2.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededExceptionV2 extends RuntimeException {
    public RateLimitExceededExceptionV2(String message) {
        super(message);
    }
}
