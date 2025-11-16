package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class UnauthorizedException extends BaseHttpException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message, int code) {
        super(message, HttpStatus.UNAUTHORIZED, code);
    }

    public UnauthorizedException(String message, String tracker) {
        super(message, HttpStatus.UNAUTHORIZED, tracker);
    }

    public UnauthorizedException(String message, int code, String tracker) {
        super(message, HttpStatus.UNAUTHORIZED, code, tracker);
    }

}
