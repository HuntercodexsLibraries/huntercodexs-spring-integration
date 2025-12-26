package com.huntercodexs.integration.handler;

import com.huntercodexs.integration.handler.interfaces.GlobalExceptionInterceptorIntegration;
import com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration;
import com.huntercodexs.integration.handler.exception.IntegrationRetryAttemptsExceededException;
import com.huntercodexs.integration.handler.exception.RateLimitExceededException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration.GENERIC_EXCEPTION_INTERCEPTOR_5XX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handlerNoInterceptor;
    private GlobalExceptionHandler handlerWithInterceptor;
    private GlobalExceptionInterceptorIntegration allEnumsInterceptor;

    @BeforeEach
    void setup() {
        handlerNoInterceptor = new GlobalExceptionHandler(new ArrayList<>());

        allEnumsInterceptor = new GlobalExceptionInterceptorIntegration() {
            @Override
            public boolean supports(GlobalEnumIntegration interceptorEnum) {
                // Supports all enums to force interceptor branch
                return true;
            }

            @Override
            public String message() {
                return "intercepted-message";
            }

            @Override
            public String trackerId() {
                return "intercepted-tracker";
            }

            @Override
            public String code() {
                return "999";
            }

            @Override
            public List<String> errors(Object exception) {
                return List.of();
            }
        };
        handlerWithInterceptor = new GlobalExceptionHandler(List.of(allEnumsInterceptor));
    }

    @Test
    void handleHttpCustomException_defaultBranch() {
        BaseHttpException ex = mock(BaseHttpException.class);
        when(ex.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);
        when(ex.getTracker()).thenReturn("custom-tracker");
        when(ex.getMessage()).thenReturn("custom-message");

        ResponseEntity<CustomResponseExceptionHandler> resp = handlerNoInterceptor.handleHttpCustomException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("custom-message", resp.getBody().getMessage());
        assertEquals("custom-tracker", resp.getBody().getTracker());
        assertEquals(String.valueOf(HttpStatus.BAD_REQUEST.value()), resp.getBody().getCode());
        assertNull(resp.getBody().getErrors());
    }

    @Test
    void handleHttpCustomException_interceptorBranch() {
        BaseHttpException ex = mock(BaseHttpException.class);
        when(ex.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);

        ResponseEntity<CustomResponseExceptionHandler> resp = handlerWithInterceptor.handleHttpCustomException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
        assertEquals("intercepted-tracker", resp.getBody().getTracker());
        assertEquals("999", resp.getBody().getCode());
        assertNull(resp.getBody().getErrors());
    }

    @Test
    void handleValidationMethodArgumentNotValidException_defaultBranch() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "target");
        result.addError(new FieldError("target", "fieldA", "must not be null"));
        result.addError(new FieldError("target", "fieldB", "must be positive"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, result);

        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleValidationMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Field argument failed", resp.getBody().getMessage());
        assertNotNull(resp.getBody().getTracker());
        assertTrue(resp.getBody().getErrors().stream().anyMatch(s -> s.contains("fieldA")));
        assertTrue(resp.getBody().getErrors().stream().anyMatch(s -> s.contains("fieldB")));
    }

    @Test
    void handleValidationMethodArgumentNotValidException_interceptorBranch() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleValidationMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
        assertEquals("999", resp.getBody().getCode());
        assertEquals("intercepted-tracker", resp.getBody().getTracker());
        assertNull(resp.getBody().getErrors());
    }

    @Test
    void handleHttpMessageNotReadableException_defaultBranch() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad json");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleHttpMessageNotReadableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Malformed JSON request", resp.getBody().getMessage());
        assertNotNull(resp.getBody().getTracker());
        assertNull(resp.getBody().getErrors());
    }

    @Test
    void handleHttpMessageNotReadableException_interceptorBranch() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad json");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleHttpMessageNotReadableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleMissingServletRequestParameterException_defaultBranch() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("p", "String");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleMissingServletRequestParameterException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().getMessage().contains("Missing required parameter"));
        assertNotNull(resp.getBody().getTracker());
    }

    @Test
    void handleMissingServletRequestParameterException_interceptorBranch() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("p", "String");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleMissingServletRequestParameterException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleConstraintViolationException_interceptorBranch() {
        ConstraintViolationException ex = new ConstraintViolationException(Set.of());
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleConstraintViolationException_defaultBranch() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(new ArrayList<>());

        jakarta.validation.Path amountPath = mock(jakarta.validation.Path.class);
        when(amountPath.toString()).thenReturn("amount");
        jakarta.validation.ConstraintViolation<?> v1 = mock(jakarta.validation.ConstraintViolation.class);
        when(v1.getPropertyPath()).thenReturn(amountPath);
        when(v1.getMessage()).thenReturn("must be positive");

        jakarta.validation.Path namePath = mock(jakarta.validation.Path.class);
        when(namePath.toString()).thenReturn("name");
        jakarta.validation.ConstraintViolation<?> v2 = mock(jakarta.validation.ConstraintViolation.class);
        when(v2.getPropertyPath()).thenReturn(namePath);
        when(v2.getMessage()).thenReturn("must not be empty");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(v2, v1));

        ResponseEntity<CustomResponseExceptionHandler> resp =
                handler.handleConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Constraint violations", resp.getBody().getMessage());
        assertNotNull(resp.getBody().getErrors());
        assertEquals(2, resp.getBody().getErrors().size());
        assertTrue(resp.getBody().getErrors().contains("amount must be positive"));
        assertTrue(resp.getBody().getErrors().contains("name must not be empty"));

        // Verifica ordenação natural
        List<String> sorted = new ArrayList<>(resp.getBody().getErrors());
        sorted.sort(Comparator.naturalOrder());
        assertEquals(sorted, resp.getBody().getErrors());
    }

    @Test
    void handleHandlerMethodValidationException_interceptorBranch() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleHandlerMethodValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleHandlerMethodValidationException_defaultBranch() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(new ArrayList<>());

        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);

        MessageSourceResolvable err1 = mock(MessageSourceResolvable.class);
        when(err1.getDefaultMessage()).thenReturn("deve ser positivo");
        when(err1.toString()).thenReturn("ObjectError: codes []; arguments []; default message [deve ser positivo]; on field amount");

        MessageSourceResolvable err2 = mock(MessageSourceResolvable.class);
        when(err2.getDefaultMessage()).thenReturn("não pode estar vazio");
        when(err2.toString()).thenReturn("ObjectError: codes []; arguments []; default message [não pode estar vazio]; on field name");

        class ValidationResultStub {
            List<MessageSourceResolvable> getResolvableErrors() {
                return List.of();
            }
        }

        ParameterValidationResult vr1 = mock(ParameterValidationResult.class);
        ParameterValidationResult vr2 = mock(ParameterValidationResult.class);
        when(vr1.getResolvableErrors()).thenReturn(List.of(err1));
        when(vr2.getResolvableErrors()).thenReturn(List.of(err2));

        when(ex.getAllValidationResults()).thenReturn(List.of(vr1, vr2));

        ResponseEntity<CustomResponseExceptionHandler> resp =
                handler.handleHandlerMethodValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Field validation failed", resp.getBody().getMessage());
        assertNotNull(resp.getBody().getTracker());

        List<String> errors = resp.getBody().getErrors();
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(s -> s.contains("deve ser positivo")));
        assertTrue(errors.stream().anyMatch(s -> s.contains("não pode estar vazio")));

    }

    @Test
    void handleHttpRequestMethodNotSupportedException_defaultBranch() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("PATCH");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleHttpRequestMethodNotSupportedException(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resp.getStatusCode());
        assertTrue(resp.getBody().getMessage().contains("not supported"));
    }

    @Test
    void handleHttpRequestMethodNotSupportedException_interceptorBranch() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("PATCH");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleHttpRequestMethodNotSupportedException(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleHttpMediaTypeNotSupportedException_defaultBranch() {
        HttpMediaTypeNotSupportedException ex =
                new HttpMediaTypeNotSupportedException("application/xml");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleHttpMediaTypeNotSupportedException(ex);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, resp.getStatusCode());
        assertTrue(resp.getBody().getMessage().contains("Media type"));
    }

    @Test
    void handleHttpMediaTypeNotSupportedException_interceptorBranch() {
        HttpMediaTypeNotSupportedException ex =
                new HttpMediaTypeNotSupportedException("application/xml");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleHttpMediaTypeNotSupportedException(ex);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleCallNotPermittedException_defaultBranch() {

        CallNotPermittedException ex = mock(CallNotPermittedException.class);
        when(ex.getMessage()).thenReturn("circuit breaker open");

        List<String> errors = java.util.stream.Stream
                .of("Service is not available", ex.getMessage())
                .filter(java.util.Objects::nonNull)
                .toList();

        when(ex.getCausingCircuitBreakerName()).thenReturn(String.valueOf(errors));

        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleCallNotPermittedException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
        assertTrue(resp.getBody().getMessage().contains("Service is not available"));
        assertNotNull(resp.getBody().getErrors());
    }

    @Test
    void handleCallNotPermittedException_interceptorBranch() {
        CallNotPermittedException ex = mock(CallNotPermittedException.class);
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleCallNotPermittedException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleRestClientException_defaultBranch() {
        RestClientException ex = new RestClientException("external failure");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleRestClientException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, resp.getStatusCode());
        assertEquals("External service communication failed", resp.getBody().getMessage());
        assertNotNull(resp.getBody().getErrors());
    }

    @Test
    void handleRestClientException_interceptorBranch() {
        RestClientException ex = new RestClientException("external failure");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleRestClientException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleDataAccessResourceFailureException_defaultBranch() {
        DataAccessResourceFailureException ex = new DataAccessResourceFailureException("db down");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleDataAccessResourceFailureException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("Communication with the database failed", resp.getBody().getMessage());
    }

    @Test
    void handleDataAccessResourceFailureException_interceptorBranch() {
        DataAccessResourceFailureException ex = new DataAccessResourceFailureException("db down");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleDataAccessResourceFailureException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleRateLimitExceededException_defaultBranch() {
        RateLimitExceededException ex = new RateLimitExceededException("too many");
        WebRequest req = mock(WebRequest.class);
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleRateLimitExceededException(ex, req);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, resp.getStatusCode());
        assertTrue(resp.getBody().getMessage().contains("Limit of requests exceeded"));
        assertNotNull(resp.getBody().getErrors());
    }

    @Test
    void handleRateLimitExceededException_interceptorBranch() {
        RateLimitExceededException ex = new RateLimitExceededException("too many");
        WebRequest req = mock(WebRequest.class);
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleRateLimitExceededException(ex, req);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleIntegrationRetryAttemptsExceededException_defaultBranch() {
        IntegrationRetryAttemptsExceededException ex =
                new IntegrationRetryAttemptsExceededException("limit exceeded", new Throwable());
        WebRequest req = mock(WebRequest.class);
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleIntegrationRetryAttemptsExceededException(ex, req);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
        assertTrue(resp.getBody().getMessage().contains("Limit of requests exceeded for Integration"));
        assertNotNull(resp.getBody().getErrors());
    }

    @Test
    void handleIntegrationRetryAttemptsExceededException_interceptorBranch() {
        IntegrationRetryAttemptsExceededException ex =
                new IntegrationRetryAttemptsExceededException("limit exceeded", new Throwable());
        WebRequest req = mock(WebRequest.class);
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleIntegrationRetryAttemptsExceededException(ex, req);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleRuntimeException_defaultBranch() {
        RuntimeException ex = new IllegalStateException("boom");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleRuntimeException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("Internal server error", resp.getBody().getMessage());
        assertNotNull(resp.getBody().getErrors());
        assertTrue(resp.getBody().getErrors().get(0).contains("IllegalStateException"));
    }

    @Test
    void handleRuntimeException_interceptorBranch() {
        RuntimeException ex = new RuntimeException("rte");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleRuntimeException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleNullPointerException_defaultBranch() {
        NullPointerException ex = new NullPointerException("npe");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleNullPointerException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("Null Pointer Exception occurred", resp.getBody().getMessage());
        assertNotNull(resp.getBody().getTracker());
        assertNotNull(resp.getBody().getErrors());
    }

    @Test
    void handleNullPointerException_interceptorBranch() {
        NullPointerException ex = new NullPointerException("npe");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleNullPointerException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
    }

    @Test
    void handleGenericException_defaultBranch() {
        Exception ex = new Exception("ex");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerNoInterceptor.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("An unexpected error occurred", resp.getBody().getMessage());
        assertNotNull(resp.getBody().getTracker());
        assertNotNull(resp.getBody().getErrors());
    }

    @Test
    void handleGenericException_interceptorBranch() {
        Exception ex = new Exception("ex");
        ResponseEntity<CustomResponseExceptionHandler> resp =
                handlerWithInterceptor.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("intercepted-message", resp.getBody().getMessage());
        assertEquals("999", resp.getBody().getCode());
        assertEquals("intercepted-tracker", resp.getBody().getTracker());
        assertNull(resp.getBody().getErrors());
    }

    @Test
    void buildErrorResponse_generatesTrackerWhenMissingAndUsesStatusCodeWhenNoCustomCode() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(new ArrayList<>());

        ResponseEntity<CustomResponseExceptionHandler> resp =
                invokeBuildErrorResponse(handler, "msg", HttpStatus.OK, null, List.of("e1"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("msg", resp.getBody().getMessage());
        assertNotNull(resp.getBody().getTracker());
        assertEquals("500", resp.getBody().getCode());
        assertEquals(List.of("e1"), resp.getBody().getErrors());
    }

    // Helper to invoke the private method via the public flow that uses it
    private ResponseEntity<CustomResponseExceptionHandler> invokeBuildErrorResponse(GlobalExceptionHandler h,
                                                                                    String message,
                                                                                    HttpStatus status,
                                                                                    String tracker,
                                                                                    List<String> errors) {
        // Use generic exception handler path to force a call with our parameters
        Exception ex = new Exception(message);
        // Use reflection is not allowed; instead simulate by temporarily setting interceptor to provide values
        GlobalExceptionInterceptorIntegration interceptor = new GlobalExceptionInterceptorIntegration() {
            @Override
            public boolean supports(GlobalEnumIntegration interceptorEnum) {
                return interceptorEnum == GENERIC_EXCEPTION_INTERCEPTOR_5XX;
            }
            @Override public String message() { return message; }
            @Override public String trackerId() { return tracker; }
            @Override public String code() { return null; } // force use of status code

            @Override
            public List<String> errors(Object exception) {
                return errors;
            }
        };
        GlobalExceptionHandler temp = new GlobalExceptionHandler(List.of(interceptor));
        return temp.handleGenericException(ex);
    }
}
