package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ServiceUnavailableException extends BaseHttpException {

    public ServiceUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String message, int code) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, code);
    }

    public ServiceUnavailableException(String message, String tracker) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, tracker);
    }

    public ServiceUnavailableException(String message, int code, String tracker) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, code, tracker);
    }

}
