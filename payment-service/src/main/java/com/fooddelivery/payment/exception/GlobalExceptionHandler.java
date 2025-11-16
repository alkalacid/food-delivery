package com.fooddelivery.payment.exception;

import com.fooddelivery.common.dto.ErrorResponse;
import com.fooddelivery.common.exception.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {
    
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException ex, HttpServletRequest request) {
        return handleNotFound(ex, request);
    }
    
    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePaymentAlreadyExists(
            PaymentAlreadyExistsException ex, HttpServletRequest request) {
        return handleConflict(ex, request);
    }
    
    @ExceptionHandler(InvalidRefundException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefund(
            InvalidRefundException ex, HttpServletRequest request) {
        return handleIllegalArgument(ex, request);
    }
}

