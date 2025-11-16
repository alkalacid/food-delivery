package com.fooddelivery.payment.dto;

import com.fooddelivery.payment.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponseDTO(
    Long id,
    Long paymentId,
    BigDecimal amount,
    String reason,
    RefundStatus status,
    String transactionId,
    Long requestedBy,
    LocalDateTime createdAt,
    LocalDateTime completedAt
) {}

