package com.fooddelivery.delivery.mapper;

import com.fooddelivery.delivery.dto.CourierRequestDTO;
import com.fooddelivery.delivery.dto.CourierResponseDTO;
import com.fooddelivery.delivery.entity.Courier;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourierMapper {
    
    CourierResponseDTO toResponse(Courier courier);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentLatitude", ignore = true)
    @Mapping(target = "currentLongitude", ignore = true)
    @Mapping(target = "lastLocationUpdate", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalDeliveries", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Courier toEntity(CourierRequestDTO request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentLatitude", ignore = true)
    @Mapping(target = "currentLongitude", ignore = true)
    @Mapping(target = "lastLocationUpdate", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalDeliveries", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(CourierRequestDTO request, @MappingTarget Courier courier);
}

