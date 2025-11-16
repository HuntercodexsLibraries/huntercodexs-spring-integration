package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class BadRequestException extends BaseHttpException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, int code) {
        super(message, HttpStatus.BAD_REQUEST, code);
    }

    public BadRequestException(String message, String tracker) {
        super(message, HttpStatus.BAD_REQUEST, tracker);
    }

    public BadRequestException(String message, int code, String tracker) {
        super(message, HttpStatus.BAD_REQUEST, code, tracker);
    }

}
