package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class UriTooLongException extends BaseHttpException {

    public UriTooLongException(String message) {
        super(message, HttpStatus.URI_TOO_LONG);
    }

    public UriTooLongException(String message, int code) {
        super(message, HttpStatus.URI_TOO_LONG, code);
    }

    public UriTooLongException(String message, String tracker) {
        super(message, HttpStatus.URI_TOO_LONG, tracker);
    }

    public UriTooLongException(String message, int code, String tracker) {
        super(message, HttpStatus.URI_TOO_LONG, code, tracker);
    }

}
