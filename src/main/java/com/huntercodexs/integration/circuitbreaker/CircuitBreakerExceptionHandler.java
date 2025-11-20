package com.huntercodexs.integration.circuitbreaker;

import com.huntercodexs.integration.handler.CustomResponseException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@ControllerAdvice
public class CircuitBreakerExceptionHandler {

    @Generated
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerExceptionHandler.class);

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<CustomResponseException> handleCallNotPermittedException(CallNotPermittedException ex) {
        String message = String.format("Service is not available '%s'", ex.getMessage());
        return buildErrorResponse(message, null, null);
    }

    private ResponseEntity<CustomResponseException> buildErrorResponse(String message, String tracker, List<String> errors) {

        if (isNull(tracker) || tracker.isEmpty()) {
            tracker = UUID.randomUUID().toString();
            log.info("No tracker provided; generated automatically: {}", tracker);
        }

        CustomResponseException response = new CustomResponseException(
                message,
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                tracker,
                errors
        );
        logException(message, HttpStatus.SERVICE_UNAVAILABLE, tracker, errors);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    private void logException(String message, HttpStatus status, String tracker, List<String> errors) {
        String logMsg = String.format("[%s] %s - %s", tracker != null ? tracker : "no-tracker", status, message);
        if (status.is5xxServerError()) {
            log.error("{} | errors={}", logMsg, errors);
        } else {
            log.warn("{} | errors={}", logMsg, errors);
        }
    }
}

