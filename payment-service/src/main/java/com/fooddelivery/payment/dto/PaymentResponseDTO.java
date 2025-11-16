package com.fooddelivery.payment.dto;

import com.fooddelivery.payment.enums.PaymentMethod;
import com.fooddelivery.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
    Long id,
    Long orderId,
    Long userId,
    BigDecimal amount,
    PaymentMethod method,
    PaymentStatus status,
    String transactionId,
    String cardLastFour,
    String failureReason,
    LocalDateTime processedAt,
    LocalDateTime createdAt
) {}

