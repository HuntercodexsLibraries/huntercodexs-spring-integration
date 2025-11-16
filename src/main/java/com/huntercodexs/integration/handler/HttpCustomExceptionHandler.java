package com.huntercodexs.integration.handler;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolationException;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.InsufficientAuthenticationException;
//import org.springframework.security.authorization.AuthorizationDeniedException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@ControllerAdvice
public class HttpCustomExceptionHandler {

    @Generated
    private static final Logger log = LoggerFactory.getLogger(HttpCustomExceptionHandler.class);

    @ExceptionHandler(BaseHttpException.class)
    public ResponseEntity<CustomResponseException> handleHttpCustomException(BaseHttpException ex) {
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), ex.getTracker(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponseException> handleValidationMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        return buildErrorResponse("Field argument failed", HttpStatus.BAD_REQUEST,  null, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomResponseException> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return buildErrorResponse("Malformed JSON request", HttpStatus.BAD_REQUEST,  null,null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomResponseException> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String message = String.format("Missing required parameter: %s", ex.getParameterName());
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST,  null,null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomResponseException> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>(List.of(ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(";")).split(";")));

        errors.sort(Comparator.naturalOrder());

        return buildErrorResponse("Constraint violations", HttpStatus.BAD_REQUEST,  null,errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<CustomResponseException> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        List<String> errors = new ArrayList<>();

        ex.getAllValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(error -> {
                    errors.add(extractTargetFromMessageError(error) +" "+ error.getDefaultMessage());
                }));

        errors.sort(Comparator.naturalOrder());

        return buildErrorResponse("Field validation failed", HttpStatus.BAD_REQUEST,  null, errors);
    }

//    @ExceptionHandler({
//            AuthenticationCredentialsNotFoundException.class,
//            AuthenticationException.class,
//            AuthenticationCredentialsNotFoundException.class,
//            BadCredentialsException.class,
//            InsufficientAuthenticationException.class
//    })
//    public ResponseEntity<CustomResponseException> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex) {
//        List<String> errors = new ArrayList<>();
//        errors.add(ex.getMessage());
//        return buildErrorResponse("Authentication required", HttpStatus.UNAUTHORIZED, null, errors);
//    }
//
//    @ExceptionHandler({
//            AccessDeniedException.class,
//            AuthorizationDeniedException.class
//    })
//    public ResponseEntity<CustomResponseException> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
//            return buildErrorResponse("Authentication required", HttpStatus.UNAUTHORIZED, null, List.of(ex.getMessage()));
//        }
//
//        return buildErrorResponse("Access denied", HttpStatus.FORBIDDEN, null, List.of(ex.getMessage()));
//    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomResponseException> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("HTTP method '%s' not supported for this endpoint", ex.getMethod());
        return buildErrorResponse(message, HttpStatus.METHOD_NOT_ALLOWED,  null, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<CustomResponseException> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String message = String.format("Media type '%s' is not supported", ex.getMessage());
        return buildErrorResponse(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE,  null, null);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<CustomResponseException> handleCallNotPermittedException(CallNotPermittedException ex) {
        String message = String.format("Service is not available '%s'", ex.getMessage());
        return buildErrorResponse(message, HttpStatus.SERVICE_UNAVAILABLE,  null, null);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<CustomResponseException> handleRestClientException(RestClientException ex) {
        log.error("External service communication failed", ex);
        return buildErrorResponse(
                "External service communication failed",
                HttpStatus.BAD_GATEWAY,
                null,
                List.of(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CustomResponseException> handleRuntimeException(RuntimeException ex) {
        log.error("Unhandled runtime exception", ex);

        return buildErrorResponse(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                null,
                List.of(ex.getClass().getSimpleName() + ": " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponseException> handleGenericException(Exception ex) {
        return buildErrorResponse(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                UUID.randomUUID().toString(),
                null
        );
    }

    private String formatFieldError(FieldError fieldError) {
        return String.format("Field '%s' %s", fieldError.getField(), fieldError.getDefaultMessage());
    }

    private String extractTargetFromMessageError(MessageSourceResolvable error) {
        try {
            return "Field "+(((error.toString()
                    .split(";")[0])
                    .split(":")[0])
                    .split("on field")[1])
                    .trim();
        } catch (RuntimeException re) {
            log.error("Something was wrong while trying to extract error from message: {}", re.getMessage());
            return re.getMessage();
        }
    }

    private ResponseEntity<CustomResponseException> buildErrorResponse(String message, HttpStatus status, String tracker, List<String> errors) {

        if (isNull(tracker) || tracker.isEmpty()) {
            tracker = UUID.randomUUID().toString();
            log.info("No tracker provided; generated automatically: {}", tracker);
        }

        CustomResponseException response = new CustomResponseException(
                message,
                status.value(),
                tracker,
                errors
        );
        logException(message, status, tracker, errors);

        return ResponseEntity.status(status).body(response);
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

