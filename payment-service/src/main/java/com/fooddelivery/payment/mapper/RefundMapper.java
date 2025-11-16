package com.fooddelivery.payment.mapper;

import com.fooddelivery.payment.dto.RefundResponseDTO;
import com.fooddelivery.payment.entity.Refund;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefundMapper {
    
    @Mapping(target = "paymentId", source = "payment.id")
    RefundResponseDTO toResponse(Refund refund);
}

