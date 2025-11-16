package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class GatewayTimeoutException extends BaseHttpException {

    public GatewayTimeoutException(String message) {
        super(message, HttpStatus.GATEWAY_TIMEOUT);
    }

    public GatewayTimeoutException(String message, int code) {
        super(message, HttpStatus.GATEWAY_TIMEOUT, code);
    }

    public GatewayTimeoutException(String message, String tracker) {
        super(message, HttpStatus.GATEWAY_TIMEOUT, tracker);
    }

    public GatewayTimeoutException(String message, int code, String tracker) {
        super(message, HttpStatus.GATEWAY_TIMEOUT, code, tracker);
    }

}
