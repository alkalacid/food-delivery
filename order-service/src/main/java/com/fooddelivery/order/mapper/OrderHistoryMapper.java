package com.fooddelivery.order.mapper;

import com.fooddelivery.order.dto.OrderHistoryResponseDTO;
import com.fooddelivery.order.entity.OrderHistory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderHistoryMapper {
    
    OrderHistoryResponseDTO toResponse(OrderHistory orderHistory);
}

