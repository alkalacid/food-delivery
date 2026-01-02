package com.fooddelivery.notification.dto;

import com.fooddelivery.notification.enums.NotificationChannel;
import com.fooddelivery.notification.enums.NotificationStatus;
import com.fooddelivery.notification.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
    Long id,
    Long userId,
    NotificationType type,
    NotificationChannel channel,
    String subject,
    String content,
    NotificationStatus status,
    String recipientEmail,
    String recipientPhone,
    String errorMessage,
    Integer retryCount,
    LocalDateTime sentAt,
    LocalDateTime createdAt
) {}

