package com.chubb.policyoverview.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chubb.policyoverview.api.dto.response.ErrorResponseDto;
import com.chubb.policyoverview.domain.exception.PolicyNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String REQUEST_PATH = "/api/v1/policies";
    private static final String HTTP_METHOD = "GET";

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(REQUEST_PATH);
        lenient().when(request.getMethod()).thenReturn(HTTP_METHOD);
    }

    @Test
    void handlePolicyNotFound_whenPolicyMissing_returnsNotFoundError() {
        UUID id = UUID.randomUUID();

        ResponseEntity<ErrorResponseDto> response =
                handler.handlePolicyNotFound(new PolicyNotFoundException(id), request);

        ErrorResponseDto body = body(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), body.getStatus());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), body.getError());
        assertEquals(REQUEST_PATH, body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void handleBodyValidation_whenFieldInvalid_returnsBadRequestWithFieldMessage() throws Exception {
        MethodArgumentNotValidException exception = methodArgumentNotValidException();

        ResponseEntity<ErrorResponseDto> response = handler.handleBodyValidation(exception, request);

        ErrorResponseDto body = body(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("policyIds: must not be empty", body.getMessage());
    }

    @Test
    void handleConstraintViolation_whenViolationPresent_returnsBadRequest() {
        ConstraintViolationException exception = new ConstraintViolationException("size must be positive", null);

        ResponseEntity<ErrorResponseDto> response = handler.handleConstraintViolation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("size must be positive", body(response).getMessage());
    }

    @Test
    void handleTypeMismatch_whenParamHasWrongType_returnsBadRequestNamingParam() {
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("oops", String.class, "status", null, null);

        ResponseEntity<ErrorResponseDto> response = handler.handleTypeMismatch(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid value for parameter 'status'.", body(response).getMessage());
    }

    @Test
    void handleUnreadableMessage_whenBodyMalformed_returnsBadRequest() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("bad json", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponseDto> response = handler.handleUnreadableMessage(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed request body.", body(response).getMessage());
    }

    @Test
    void handleUnexpected_whenUnknownErrorOccurs_returnsInternalServerError() {
        ResponseEntity<ErrorResponseDto> response =
                handler.handleUnexpected(new IllegalStateException("boom"), request);

        ErrorResponseDto body = body(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred.", body.getMessage());
        assertEquals(REQUEST_PATH, body.getPath());
    }

    private MethodArgumentNotValidException methodArgumentNotValidException() throws NoSuchMethodException {
        MethodParameter parameter =
                new MethodParameter(GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", List.class), 0);
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "policyIds", "must not be empty"));
        return new MethodArgumentNotValidException(parameter, bindingResult);
    }

    @SuppressWarnings("unused")
    private void dummy(List<UUID> policyIds) {
    }

    private ErrorResponseDto body(ResponseEntity<ErrorResponseDto> response) {
        ErrorResponseDto body = response.getBody();
        assertNotNull(body);
        return body;
    }
}
