package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class BadGatewayException extends BaseHttpException {

    public BadGatewayException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }

    public BadGatewayException(String message, int code) {
        super(message, HttpStatus.BAD_GATEWAY, code);
    }

    public BadGatewayException(String message, String tracker) {
        super(message, HttpStatus.BAD_GATEWAY, tracker);
    }

    public BadGatewayException(String message, int code, String tracker) {
        super(message, HttpStatus.BAD_GATEWAY, code, tracker);
    }
}
