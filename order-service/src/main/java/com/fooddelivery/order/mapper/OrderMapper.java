package com.fooddelivery.order.mapper;

import com.fooddelivery.order.dto.OrderResponseDTO;
import com.fooddelivery.order.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    
    OrderResponseDTO toResponse(Order order);
}

