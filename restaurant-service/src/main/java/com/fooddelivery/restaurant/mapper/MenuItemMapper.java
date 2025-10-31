package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.MenuItemRequestDTO;
import com.fooddelivery.restaurant.dto.MenuItemResponseDTO;
import com.fooddelivery.restaurant.entity.MenuItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuItemMapper {
    
    @Mapping(source = "restaurant.id", target = "restaurantId")
    MenuItemResponseDTO toResponse(MenuItem menuItem);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MenuItem toEntity(MenuItemRequestDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(MenuItemRequestDTO dto, @MappingTarget MenuItem menuItem);
}

