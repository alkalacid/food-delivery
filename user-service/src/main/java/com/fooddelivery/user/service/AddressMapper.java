package com.fooddelivery.user.service;

import com.fooddelivery.user.dto.AddressResponseDTO;
import com.fooddelivery.user.entity.UserAddress;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressMapper {
    AddressResponseDTO toResponse(UserAddress address);
}

