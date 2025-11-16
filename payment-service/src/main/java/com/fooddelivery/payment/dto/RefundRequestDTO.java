package com.fooddelivery.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RefundRequestDTO(
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    BigDecimal amount,
    
    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must be less than 500 characters")
    String reason
) {}

