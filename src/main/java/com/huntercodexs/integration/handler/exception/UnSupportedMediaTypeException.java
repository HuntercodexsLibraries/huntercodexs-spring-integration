package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class UnSupportedMediaTypeException extends BaseHttpException {

    public UnSupportedMediaTypeException(String message) {
        super(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    public UnSupportedMediaTypeException(String message, int code) {
        super(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE, code);
    }

    public UnSupportedMediaTypeException(String message, String tracker) {
        super(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE, tracker);
    }

    public UnSupportedMediaTypeException(String message, int code, String tracker) {
        super(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE, code, tracker);
    }

}
