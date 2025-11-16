package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class InternalServerException extends BaseHttpException {

    public InternalServerException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerException(String message, int code) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, code);
    }

    public InternalServerException(String message, String tracker) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, tracker);
    }

    public InternalServerException(String message, int code, String tracker) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, code, tracker);
    }

}
