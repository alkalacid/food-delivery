package com.fooddelivery.order.mapper;

import com.fooddelivery.order.dto.PromoCodeRequestDTO;
import com.fooddelivery.order.dto.PromoCodeResponseDTO;
import com.fooddelivery.order.entity.PromoCode;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromoCodeMapper {

    PromoCodeResponseDTO toResponse(PromoCode promoCode);

    PromoCode toEntity(PromoCodeRequestDTO request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(PromoCodeRequestDTO request, @MappingTarget PromoCode promoCode);
}

