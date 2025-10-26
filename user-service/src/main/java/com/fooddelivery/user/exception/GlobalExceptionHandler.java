package com.fooddelivery.user.exception;

import com.fooddelivery.common.dto.ErrorResponse;
import com.fooddelivery.common.exception.BaseGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        return handleConflict(ex, request);
    }
    
    @ExceptionHandler({UserNotFoundException.class, AddressNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            RuntimeException ex, HttpServletRequest request) {
        return handleNotFound(ex, request);
    }
    
    @ExceptionHandler(AddressLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleAddressLimitExceeded(
            AddressLimitExceededException ex, HttpServletRequest request) {
        return handleIllegalArgument(ex, request);
    }
    
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationErrors(
            Exception ex, HttpServletRequest request) {
        return handleUnauthorized(ex, request, "Invalid credentials");
    }
}

