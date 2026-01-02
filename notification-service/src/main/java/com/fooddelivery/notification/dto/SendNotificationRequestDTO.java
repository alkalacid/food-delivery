package com.fooddelivery.notification.dto;

import com.fooddelivery.notification.enums.NotificationChannel;
import com.fooddelivery.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendNotificationRequestDTO(
    @NotNull(message = "User ID is required")
    Long userId,
    
    @NotNull(message = "Notification type is required")
    NotificationType type,
    
    @NotNull(message = "Channel is required")
    NotificationChannel channel,
    
    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must be less than 200 characters")
    String subject,
    
    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content must be less than 2000 characters")
    String content,
    
    @Size(max = 255, message = "Email must be less than 255 characters")
    String recipientEmail,
    
    @Size(max = 20, message = "Phone must be less than 20 characters")
    String recipientPhone
) {}

