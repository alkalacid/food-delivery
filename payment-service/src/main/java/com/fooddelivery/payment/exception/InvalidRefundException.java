package com.fooddelivery.payment.exception;

public class InvalidRefundException extends IllegalArgumentException {
    public InvalidRefundException(String message) {
        super(message);
    }
}

