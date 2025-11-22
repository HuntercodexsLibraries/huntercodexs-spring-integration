package com.huntercodexs.integration.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class IntegrationRetryAttemptsExceededException extends RuntimeException {
    public IntegrationRetryAttemptsExceededException(String message, Throwable cause) {
        super(message, cause);
    }

}
