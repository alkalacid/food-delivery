package com.fooddelivery.order.exception;

public class PromoCodeNotFoundException extends RuntimeException {
    public PromoCodeNotFoundException(String message) {
        super(message);
    }
}

