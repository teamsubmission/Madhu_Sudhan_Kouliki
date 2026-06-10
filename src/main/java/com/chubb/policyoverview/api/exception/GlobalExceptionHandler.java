package com.chubb.policyoverview.api.exception;

import com.chubb.policyoverview.api.dto.response.ErrorResponseDto;
import com.chubb.policyoverview.domain.exception.PolicyNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String UNKNOWN_CORRELATION_ID = "N/A";
    private static final String VALIDATION_FAILED_MESSAGE = "Request validation failed.";
    private static final String MALFORMED_REQUEST_MESSAGE = "Malformed request body.";
    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred.";
    private static final String FIELD_ERROR_TEMPLATE = "%s: %s";
    private static final String INVALID_PARAM_TEMPLATE = "Invalid value for parameter '%s'.";
    private static final String CLIENT_ERROR_LOG =
            "Request failed - method={}, path={}, correlationId={}, status={}, message={}";
    private static final String SERVER_ERROR_LOG =
            "Request failed - method={}, path={}, correlationId={}, status={}";

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handlePolicyNotFound(
            PolicyNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleBodyValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(this::formatFieldError)
                .orElse(VALIDATION_FAILED_MESSAGE);
        return build(HttpStatus.BAD_REQUEST, message, request, exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        String message = Optional.ofNullable(exception.getMessage()).orElse(VALIDATION_FAILED_MESSAGE);
        return build(HttpStatus.BAD_REQUEST, message, request, exception);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        String message = String.format(INVALID_PARAM_TEMPLATE, exception.getName());
        return build(HttpStatus.BAD_REQUEST, message, request, exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleUnreadableMessage(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, MALFORMED_REQUEST_MESSAGE, request, exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(
            Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_MESSAGE, request, exception);
    }

    private String formatFieldError(FieldError fieldError) {
        return String.format(FIELD_ERROR_TEMPLATE, fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponseDto> build(
            HttpStatus status, String message, HttpServletRequest request, Exception exception) {
        logFailure(status, message, request, exception);
        ErrorResponseDto body = ErrorResponseDto.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private void logFailure(
            HttpStatus status, String message, HttpServletRequest request, Exception exception) {
        String correlationId = Optional.ofNullable(MDC.get(CORRELATION_ID_KEY)).orElse(UNKNOWN_CORRELATION_ID);
        if (status.is5xxServerError()) {
            log.error(SERVER_ERROR_LOG, request.getMethod(), request.getRequestURI(),
                    correlationId, status.value(), exception);
            return;
        }
        log.warn(CLIENT_ERROR_LOG, request.getMethod(), request.getRequestURI(),
                correlationId, status.value(), message);
    }
}
