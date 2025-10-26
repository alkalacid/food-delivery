package com.fooddelivery.user.service;

import com.fooddelivery.user.dto.UserResponseDTO;
import com.fooddelivery.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserResponseDTO toResponse(User user);
}

