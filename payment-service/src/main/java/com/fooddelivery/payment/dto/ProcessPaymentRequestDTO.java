package com.fooddelivery.payment.dto;

import com.fooddelivery.payment.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProcessPaymentRequestDTO(
    @NotNull(message = "Order ID is required")
    Long orderId,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @NotNull(message = "Payment method is required")
    PaymentMethod method,
    
    @Size(max = 4, message = "Card last four must be 4 digits")
    String cardLastFour
) {}

