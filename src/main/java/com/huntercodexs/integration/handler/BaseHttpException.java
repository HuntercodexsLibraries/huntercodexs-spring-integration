package com.huntercodexs.integration.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

@Getter
public abstract class BaseHttpException extends RestClientException {

    private final int code;
    private final String tracker;
    private final HttpStatus status;

    protected BaseHttpException(String message, HttpStatus status, int code, String tracker) {
        super(message);
        this.status = status;
        this.code = code;
        this.tracker = tracker;
    }

    protected BaseHttpException(String message, HttpStatus status) {
        this(message, status, status.value(), null);
    }

    protected BaseHttpException(String message, HttpStatus status, int code) {
        this(message, status, code, null);
    }

    protected BaseHttpException(String message, HttpStatus status, String tracker) {
        this(message, status, status.value(), tracker);
    }
}

