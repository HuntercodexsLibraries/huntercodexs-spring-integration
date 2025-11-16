package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class TooManyRequestsException extends BaseHttpException {

    public TooManyRequestsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }

    public TooManyRequestsException(String message, int code) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, code);
    }

    public TooManyRequestsException(String message, String tracker) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, tracker);
    }

    public TooManyRequestsException(String message, int code, String tracker) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, code, tracker);
    }

}
