package com.fooddelivery.delivery.exception;

public class CourierAlreadyExistsException extends RuntimeException {
    public CourierAlreadyExistsException(String message) {
        super(message);
    }
}

