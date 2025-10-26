package com.fooddelivery.user.dto;

public record AuthResponseDTO(
    String accessToken,
    String tokenType,
    Long expiresIn,
    UserResponseDTO user
) {
    public AuthResponseDTO(String accessToken, Long expiresIn, UserResponseDTO user) {
        this(accessToken, "Bearer", expiresIn, user);
    }
}

