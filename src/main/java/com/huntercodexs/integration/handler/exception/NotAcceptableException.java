package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class NotAcceptableException extends BaseHttpException {

    public NotAcceptableException(String message) {
        super(message, HttpStatus.NOT_ACCEPTABLE);
    }

    public NotAcceptableException(String message, int code) {
        super(message, HttpStatus.NOT_ACCEPTABLE, code);
    }

    public NotAcceptableException(String message, String tracker) {
        super(message, HttpStatus.NOT_ACCEPTABLE, tracker);
    }

    public NotAcceptableException(String message, int code, String tracker) {
        super(message, HttpStatus.NOT_ACCEPTABLE, code, tracker);
    }

}
