package com.fooddelivery.restaurant.exception;

import com.fooddelivery.common.dto.ErrorResponse;
import com.fooddelivery.common.exception.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {
    
    @ExceptionHandler({RestaurantNotFoundException.class, MenuItemNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            RuntimeException ex, HttpServletRequest request) {
        return handleNotFound(ex, request);
    }
    
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, HttpServletRequest request) {
        return handleForbidden(ex, request);
    }
    
    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateReview(
            DuplicateReviewException ex, HttpServletRequest request) {
        return handleConflict(ex, request);
    }
}

