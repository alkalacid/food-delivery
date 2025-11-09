package com.fooddelivery.order.mapper;

import com.fooddelivery.order.dto.OrderItemResponseDTO;
import com.fooddelivery.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {
    
    OrderItemResponseDTO toResponse(OrderItem orderItem);
}

