package com.huntercodexs.integration.ratelimit.v2.handler;

import com.huntercodexs.integration.ratelimit.v2.handler.exception.RateLimitExceededExceptionV2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandlerV2 {

    @ExceptionHandler(RateLimitExceededExceptionV2.class)
    public ResponseEntity<Object> handleRateLimitExceededException(RateLimitExceededExceptionV2 ex, WebRequest request) {
        String errorMessage = "Limit of requests exceeded. Please try again later.";
        return new ResponseEntity<>(errorMessage, HttpStatus.TOO_MANY_REQUESTS);
    }
}
