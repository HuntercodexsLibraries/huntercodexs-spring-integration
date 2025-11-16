package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ForbiddenException extends BaseHttpException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message, int code) {
        super(message, HttpStatus.FORBIDDEN, code);
    }

    public ForbiddenException(String message, String tracker) {
        super(message, HttpStatus.FORBIDDEN, tracker);
    }

    public ForbiddenException(String message, int code, String tracker) {
        super(message, HttpStatus.FORBIDDEN, code, tracker);
    }

}
