package com.fooddelivery.notification.mapper;

import com.fooddelivery.notification.dto.NotificationResponseDTO;
import com.fooddelivery.notification.dto.SendNotificationRequestDTO;
import com.fooddelivery.notification.entity.Notification;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {
    
    NotificationResponseDTO toResponse(Notification notification);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "retryCount", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Notification toEntity(SendNotificationRequestDTO request);
}

