package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends BaseHttpException {

    public CustomException(String message, HttpStatus status) {
        super(message, status);
    }

    public CustomException(String message, HttpStatus status, int code) {
        super(message, status, code);
    }

    public CustomException(String message, HttpStatus status, String tracker) {
        super(message, status, tracker);
    }

    public CustomException(String message, HttpStatus status, int code, String tracker) {
        super(message, status, code, tracker);
    }

}
