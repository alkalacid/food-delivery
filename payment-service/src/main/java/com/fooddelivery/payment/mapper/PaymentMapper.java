package com.fooddelivery.payment.mapper;

import com.fooddelivery.payment.dto.PaymentResponseDTO;
import com.fooddelivery.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {
    
    PaymentResponseDTO toResponse(Payment payment);
}

