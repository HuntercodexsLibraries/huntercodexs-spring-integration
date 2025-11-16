package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class MethodNotAllowedException extends BaseHttpException {

    public MethodNotAllowedException(String message) {
        super(message, HttpStatus.METHOD_NOT_ALLOWED);
    }

    public MethodNotAllowedException(String message, int code) {
        super(message, HttpStatus.METHOD_NOT_ALLOWED, code);
    }

    public MethodNotAllowedException(String message, String tracker) {
        super(message, HttpStatus.METHOD_NOT_ALLOWED, tracker);
    }

    public MethodNotAllowedException(String message, int code, String tracker) {
        super(message, HttpStatus.METHOD_NOT_ALLOWED, code, tracker);
    }

}
