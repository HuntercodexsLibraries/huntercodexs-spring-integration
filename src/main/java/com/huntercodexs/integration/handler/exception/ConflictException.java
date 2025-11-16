package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ConflictException extends BaseHttpException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public ConflictException(String message, int code) {
        super(message, HttpStatus.CONFLICT, code);
    }

    public ConflictException(String message, String tracker) {
        super(message, HttpStatus.CONFLICT, tracker);
    }

    public ConflictException(String message, int code, String tracker) {
        super(message, HttpStatus.CONFLICT, code, tracker);
    }

}
