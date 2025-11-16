package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class NotImplementedException extends BaseHttpException {

    public NotImplementedException(String message) {
        super(message, HttpStatus.NOT_IMPLEMENTED);
    }

    public NotImplementedException(String message, int code) {
        super(message, HttpStatus.NOT_IMPLEMENTED, code);
    }

    public NotImplementedException(String message, String tracker) {
        super(message, HttpStatus.NOT_IMPLEMENTED, tracker);
    }

    public NotImplementedException(String message, int code, String tracker) {
        super(message, HttpStatus.NOT_IMPLEMENTED, code, tracker);
    }

}
