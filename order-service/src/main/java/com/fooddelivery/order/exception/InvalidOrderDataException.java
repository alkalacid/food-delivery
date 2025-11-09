package com.fooddelivery.order.exception;

public class InvalidOrderDataException extends RuntimeException {
    public InvalidOrderDataException(String message) {
        super(message);
    }
}

