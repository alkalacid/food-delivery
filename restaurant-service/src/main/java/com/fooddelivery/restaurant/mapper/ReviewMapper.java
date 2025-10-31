package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.ReviewRequestDTO;
import com.fooddelivery.restaurant.dto.ReviewResponseDTO;
import com.fooddelivery.restaurant.entity.Review;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    
    @Mapping(source = "restaurant.id", target = "restaurantId")
    ReviewResponseDTO toResponse(Review review);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toEntity(ReviewRequestDTO dto);
}

