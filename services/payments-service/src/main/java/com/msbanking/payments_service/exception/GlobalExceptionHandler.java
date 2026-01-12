package com.msbanking.payments_service.exception;

import com.msbanking.commons.exception.BusinessException;
import com.msbanking.commons.exception.ErrorCode;
import com.msbanking.commons.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.error("Business exception: {} - {}", ex.getErrorCode().getCode(), ex.getCustomMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getCustomMessage(), request.getRequestURI());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ErrorCode.VALIDATION_ERROR.getHttpStatus().value())
                .error(ErrorCode.VALIDATION_ERROR.getHttpStatus().getReasonPhrase())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception: ", ex);
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(errorResponse);
    }
}
