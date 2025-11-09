package com.fooddelivery.order.exception;

import com.fooddelivery.common.dto.ErrorResponse;
import com.fooddelivery.common.exception.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex, HttpServletRequest request) {
        return handleNotFound(ex, request);
    }
    
    @ExceptionHandler({InvalidOrderStateException.class, InvalidOrderDataException.class})
    public ResponseEntity<ErrorResponse> handleInvalidOrder(
            RuntimeException ex, HttpServletRequest request) {
        return handleIllegalArgument((IllegalArgumentException) ex, request);
    }
    
    @ExceptionHandler(UnauthorizedOrderAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedOrderAccessException ex, HttpServletRequest request) {
        return handleForbidden(ex, request);
    }

    @ExceptionHandler(InvalidPromoCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPromoCode(
        IllegalArgumentException ex, HttpServletRequest request) {
        return handleIllegalArgument(ex, request);
    }

    @ExceptionHandler(PromoCodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePromoCodeNotFound(
            PromoCodeNotFoundException ex, HttpServletRequest request) {
        return handleNotFound(ex, request);
    }
}

