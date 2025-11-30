package com.fooddelivery.delivery.mapper;

import com.fooddelivery.delivery.dto.CreateDeliveryRequestDTO;
import com.fooddelivery.delivery.dto.DeliveryResponseDTO;
import com.fooddelivery.delivery.entity.Delivery;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = CourierMapper.class)
public interface DeliveryMapper {
    
    DeliveryResponseDTO toResponse(Delivery delivery);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courier", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "estimatedDistanceMeters", ignore = true)
    @Mapping(target = "estimatedTimeMinutes", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "pickedUpAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Delivery toEntity(CreateDeliveryRequestDTO request);
}

