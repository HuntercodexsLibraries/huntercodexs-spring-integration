package com.huntercodexs.integration.handler;

import com.huntercodexs.integration.core.interfaces.GlobalExceptionInterceptorIntegration;
import com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration;
import com.huntercodexs.integration.handler.exception.IntegrationRetryAttemptsExceededException;
import com.huntercodexs.integration.handler.exception.RateLimitExceededException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolationException;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration.*;
import static java.util.Objects.isNull;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Generated
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String message = null;
    private String tracker = null;
    private String code = null;
    private List<String> errors = new ArrayList<>();

    private final List<GlobalExceptionInterceptorIntegration> interceptors;

    public GlobalExceptionHandler(List<GlobalExceptionInterceptorIntegration> interceptors) {
        this.interceptors = interceptors;
    }

    @ExceptionHandler(BaseHttpException.class)
    public ResponseEntity<CustomResponseException> handleHttpCustomException(BaseHttpException ex) {
        if (getInterceptor(CUSTOM_EXCEPTION_INTERCEPTOR, ex)) {
            return buildErrorResponse(this.message, ex.getStatus(), this.tracker, this.errors);
        }
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), ex.getTracker(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponseException> handleValidationMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        if (getInterceptor(METHOD_ARGUMENT_VALIDATION_EXCEPTION_INTERCEPTOR_400, ex)) {
            return buildErrorResponse(this.message, HttpStatus.BAD_REQUEST, this.tracker, this.errors);
        }

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        return buildErrorResponse("Field argument failed", HttpStatus.BAD_REQUEST,  null, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomResponseException> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        if (getInterceptor(HTTP_MESSAGE_NOT_READABLE_EXCEPTION_INTERCEPTOR_400, ex)) {
            return buildErrorResponse(this.message, HttpStatus.BAD_REQUEST, this.tracker, this.errors);
        }
        return buildErrorResponse("Malformed JSON request", HttpStatus.BAD_REQUEST,  null,null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomResponseException> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        if (getInterceptor(MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION_INTERCEPTOR_400, ex)) {
            return buildErrorResponse(this.message, HttpStatus.BAD_REQUEST, this.tracker, this.errors);
        }

        String message = String.format("Missing required parameter: %s", ex.getParameterName());
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST,  null,null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomResponseException> handleConstraintViolationException(ConstraintViolationException ex) {
        if (getInterceptor(CONSTRAINT_VIOLATION_EXCEPTION_INTERCEPTOR_400, ex)) {
            return buildErrorResponse(this.message, HttpStatus.BAD_REQUEST, this.tracker, this.errors);
        }

        List<String> errors = new ArrayList<>(List.of(ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(";")).split(";")));

        errors.sort(Comparator.naturalOrder());

        return buildErrorResponse("Constraint violations", HttpStatus.BAD_REQUEST,  null,errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<CustomResponseException> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        if (getInterceptor(HANDLER_METHOD_VALIDATION_EXCEPTION_INTERCEPTOR_400, ex)) {
            return buildErrorResponse(this.message, HttpStatus.BAD_REQUEST, this.tracker, this.errors);
        }

        List<String> errors = new ArrayList<>();

        ex.getAllValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(error -> {
                    errors.add(extractTargetFromMessageError(error) +" "+ error.getDefaultMessage());
                }));

        errors.sort(Comparator.naturalOrder());

        return buildErrorResponse("Field validation failed", HttpStatus.BAD_REQUEST,  null, errors);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomResponseException> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        if (getInterceptor(HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION_INTERCEPTOR_405, ex)) {
            return buildErrorResponse(this.message, HttpStatus.METHOD_NOT_ALLOWED, this.tracker, this.errors);
        }

        String message = String.format("HTTP method '%s' not supported for this endpoint", ex.getMethod());
        return buildErrorResponse(message, HttpStatus.METHOD_NOT_ALLOWED,  null, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<CustomResponseException> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        if (getInterceptor(HTTP_MEDIA_TYPE_NOT_SUPPORTED_EXCEPTION_INTERCEPTOR_415, ex)) {
            return buildErrorResponse(this.message, HttpStatus.UNSUPPORTED_MEDIA_TYPE, this.tracker, this.errors);
        }

        String message = String.format("Media type '%s' is not supported", ex.getMessage());
        return buildErrorResponse(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE,  null, null);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<CustomResponseException> handleCallNotPermittedException(CallNotPermittedException ex) {
        if (getInterceptor(CIRCUIT_BREAKER_CALL_NOT_PERMITTED_EXCEPTION_INTERCEPTOR_503, ex)) {
            return buildErrorResponse(this.message, HttpStatus.SERVICE_UNAVAILABLE, this.tracker, this.errors);
        }

        String message = String.format("Service is not available '%s'", ex.getMessage());
        return buildErrorResponse(message, HttpStatus.SERVICE_UNAVAILABLE,  null, List.of(ex.getMessage(), ex.getCausingCircuitBreakerName()));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<CustomResponseException> handleRestClientException(RestClientException ex) {
        if (getInterceptor(REST_CLIENT_EXCEPTION_INTERCEPTOR_502, ex)) {
            return buildErrorResponse(this.message, HttpStatus.BAD_GATEWAY, this.tracker, this.errors);
        }

        log.error("External service communication failed", ex);
        return buildErrorResponse(
                "External service communication failed",
                HttpStatus.BAD_GATEWAY,
                null,
                List.of(ex.getMessage()));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<CustomResponseException> handleDataAccessResourceFailureException(DataAccessResourceFailureException ex) {
        if (getInterceptor(DATA_ACCESS_RESOURCE_FAILURE_EXCEPTION_INTERCEPTOR_500, ex)) {
            return buildErrorResponse(this.message, HttpStatus.INTERNAL_SERVER_ERROR, this.tracker, this.errors);
        }

        log.error("Communication with the database failed", ex);
        return buildErrorResponse(
                "Communication with the database failed",
                HttpStatus.INTERNAL_SERVER_ERROR,
                null,
                null);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<CustomResponseException> handleRateLimitExceededException(RateLimitExceededException ex, WebRequest request) {
        if (getInterceptor(RATE_LIMIT_EXCEEDED_EXCEPTION_INTERCEPTOR_429, ex)) {
            return buildErrorResponse(this.message, HttpStatus.TOO_MANY_REQUESTS, this.tracker, this.errors);
        }

        log.error("Limit of requests exceeded. Please try again later.", ex);
        return buildErrorResponse(
                "Limit of requests exceeded. Please try again later.",
                HttpStatus.TOO_MANY_REQUESTS,
                null,
                List.of(ex.getMessage()));
    }

    @ExceptionHandler(IntegrationRetryAttemptsExceededException.class)
    public ResponseEntity<CustomResponseException> handleIntegrationRetryAttemptsExceededException(IntegrationRetryAttemptsExceededException ex, WebRequest request) {
        if (getInterceptor(INTEGRATION_RETRY_EXCEEDED_EXCEPTION_INTERCEPTOR_503, ex)) {
            return buildErrorResponse(this.message, HttpStatus.SERVICE_UNAVAILABLE, this.tracker, this.errors);
        }

        log.error("Limit of requests exceeded for Integration: {}", ex.getMessage());
        return buildErrorResponse(
                "Limit of requests exceeded for Integration",
                HttpStatus.SERVICE_UNAVAILABLE,
                null,
                List.of(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CustomResponseException> handleRuntimeException(RuntimeException ex) {
        if (getInterceptor(RUNTIME_EXCEPTION_INTERCEPTOR_500, ex)) {
            return buildErrorResponse(this.message, HttpStatus.INTERNAL_SERVER_ERROR, this.tracker, this.errors);
        }

        log.error("Unhandled runtime exception", ex);

        return buildErrorResponse(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                null,
                List.of(ex.getClass().getSimpleName(), ex.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<CustomResponseException> handleNullPointerException(NullPointerException ex) {
        if (getInterceptor(NULL_POINTER_EXCEPTION_INTERCEPTOR_5XX, ex)) {
            return buildErrorResponse(this.message, HttpStatus.INTERNAL_SERVER_ERROR, this.tracker, this.errors);
        }

        return buildErrorResponse(
                "Null Pointer Exception occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                UUID.randomUUID().toString(),
                List.of(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponseException> handleGenericException(Exception ex) {
        if (getInterceptor(GENERIC_EXCEPTION_INTERCEPTOR_5XX, ex)) {
            return buildErrorResponse(this.message, HttpStatus.INTERNAL_SERVER_ERROR, this.tracker, this.errors);
        }

        return buildErrorResponse(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                UUID.randomUUID().toString(),
                List.of(ex.getClass().getSimpleName(), ex.getMessage()));
    }

    private boolean getInterceptor(GlobalEnumIntegration interceptorEnum, Exception ex) {
        GlobalExceptionInterceptorIntegration interceptor = interceptors.stream()
                .filter(r -> r.supports(interceptorEnum))
                .findFirst()
                .orElse(null);

        if (interceptor != null) {
            this.message = interceptor.message();
            this.tracker = interceptor.trackerId();
            this.code = interceptor.code();
            this.errors = interceptor.errors(ex);
            return true;
        }

        return false;
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

        String overCode = this.code != null ? this.code : String.valueOf(status.value());

        CustomResponseException response = new CustomResponseException(message, overCode, tracker, errors);

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

