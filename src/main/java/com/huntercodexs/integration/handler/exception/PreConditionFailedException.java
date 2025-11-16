package com.huntercodexs.integration.handler.exception;

import com.huntercodexs.integration.handler.BaseHttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class PreConditionFailedException extends BaseHttpException {

    public PreConditionFailedException(String message) {
        super(message, HttpStatus.PRECONDITION_FAILED);
    }

    public PreConditionFailedException(String message, int code) {
        super(message, HttpStatus.PRECONDITION_FAILED, code);
    }

    public PreConditionFailedException(String message, String tracker) {
        super(message, HttpStatus.PRECONDITION_FAILED, tracker);
    }

    public PreConditionFailedException(String message, int code, String tracker) {
        super(message, HttpStatus.PRECONDITION_FAILED, code, tracker);
    }

}
