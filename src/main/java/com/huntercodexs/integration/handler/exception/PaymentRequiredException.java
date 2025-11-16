package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class PaymentRequiredException extends BaseHttpException {

    public PaymentRequiredException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED);
    }

    public PaymentRequiredException(String message, int code) {
        super(message, HttpStatus.PAYMENT_REQUIRED, code);
    }

    public PaymentRequiredException(String message, String tracker) {
        super(message, HttpStatus.PAYMENT_REQUIRED, tracker);
    }

    public PaymentRequiredException(String message, int code, String tracker) {
        super(message, HttpStatus.PAYMENT_REQUIRED, code, tracker);
    }

}
