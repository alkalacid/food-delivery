package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.RestaurantRequestDTO;
import com.fooddelivery.restaurant.dto.RestaurantResponseDTO;
import com.fooddelivery.restaurant.entity.Restaurant;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RestaurantMapper {
    
    RestaurantResponseDTO toResponse(Restaurant restaurant);
    
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "ownerId", ignore = true),
        @Mapping(target = "averageRating", ignore = true),
        @Mapping(target = "totalReviews", ignore = true),
        @Mapping(target = "menuItems", ignore = true),
        @Mapping(target = "reviews", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "deletedAt", ignore = true)
    })
    Restaurant toEntity(RestaurantRequestDTO dto);
    
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "ownerId", ignore = true),
        @Mapping(target = "averageRating", ignore = true),
        @Mapping(target = "totalReviews", ignore = true),
        @Mapping(target = "menuItems", ignore = true),
        @Mapping(target = "reviews", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "deletedAt", ignore = true)
    })
    void updateEntity(RestaurantRequestDTO dto, @MappingTarget Restaurant restaurant);
}

