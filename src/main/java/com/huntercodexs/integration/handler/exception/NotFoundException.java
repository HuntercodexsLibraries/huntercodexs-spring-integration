package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class NotFoundException extends BaseHttpException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message, int code) {
        super(message, HttpStatus.NOT_FOUND, code);
    }

    public NotFoundException(String message, String tracker) {
        super(message, HttpStatus.NOT_FOUND, tracker);
    }

    public NotFoundException(String message, int code, String tracker) {
        super(message, HttpStatus.NOT_FOUND, code, tracker);
    }

}
