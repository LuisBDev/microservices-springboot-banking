package com.msbanking.commons.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Generic errors (GEN)
    INTERNAL_SERVER_ERROR("GEN-001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("GEN-002", "Validation failed", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("GEN-003", "Resource not found", HttpStatus.NOT_FOUND),
    BAD_REQUEST("GEN-004", "Bad request", HttpStatus.BAD_REQUEST),
    CONFLICT("GEN-005", "Resource conflict", HttpStatus.CONFLICT),

    // Order errors (ORD)
    ORDER_NOT_FOUND("ORD-001", "Order not found", HttpStatus.NOT_FOUND),
    ORDER_PROCESSING_FAILED("ORD-002", "Order processing failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_ALREADY_COMPLETED("ORD-003", "Cannot cancel a completed order", HttpStatus.CONFLICT),
    ORDER_ALREADY_CANCELLED("ORD-004", "Order is already cancelled", HttpStatus.CONFLICT),

    // User errors (USR)
    USER_NOT_FOUND("USR-001", "User not found", HttpStatus.NOT_FOUND),
    DUPLICATE_USER("USR-002", "User already exists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("USR-003", "Invalid credentials", HttpStatus.UNAUTHORIZED),

    // Inventory/Product errors (INV)
    PRODUCT_NOT_FOUND("INV-001", "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_ACTIVE("INV-002", "Product is not active", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK("INV-003", "Insufficient stock available", HttpStatus.CONFLICT),
    PRODUCT_VALIDATION_FAILED("INV-004", "Product validation failed", HttpStatus.BAD_REQUEST),
    DUPLICATE_PRODUCT("INV-005", "Product with this SKU already exists", HttpStatus.CONFLICT),

    // Payment errors (PAY)
    PAYMENT_PROCESSING_FAILED("PAY-001", "Payment processing failed", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_NOT_FOUND("PAY-002", "Payment not found", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_METHOD("PAY-003", "Invalid payment method", HttpStatus.BAD_REQUEST),
    PAYMENT_DECLINED("PAY-004", "Payment was declined", HttpStatus.PAYMENT_REQUIRED),
    PAYMENT_NOT_COMPLETED_FOR_REFUND("PAY-005", "Only completed payments can be refunded", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
