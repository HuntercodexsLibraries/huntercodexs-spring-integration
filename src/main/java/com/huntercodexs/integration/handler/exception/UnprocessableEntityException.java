package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class UnprocessableEntityException extends BaseHttpException {

    public UnprocessableEntityException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public UnprocessableEntityException(String message, int code) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, code);
    }

    public UnprocessableEntityException(String message, String tracker) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, tracker);
    }

    public UnprocessableEntityException(String message, int code, String tracker) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, code, tracker);
    }

}
