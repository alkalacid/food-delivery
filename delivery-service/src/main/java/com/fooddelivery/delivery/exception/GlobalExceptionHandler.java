package com.fooddelivery.delivery.exception;

import com.fooddelivery.common.dto.ErrorResponse;
import com.fooddelivery.common.exception.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {
    
    @ExceptionHandler({CourierNotFoundException.class, DeliveryNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(
            RuntimeException ex, HttpServletRequest request) {
        return super.handleNotFound(ex, request);
    }
    
    @ExceptionHandler({CourierAlreadyExistsException.class, DeliveryAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleAlreadyExistsExceptions(
            RuntimeException ex, HttpServletRequest request) {
        return super.handleConflict(ex, request);
    }
    
    @ExceptionHandler(InvalidDeliveryStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateException(
            InvalidDeliveryStateException ex, HttpServletRequest request) {
        return super.handleIllegalArgument(ex, request);
    }
}

