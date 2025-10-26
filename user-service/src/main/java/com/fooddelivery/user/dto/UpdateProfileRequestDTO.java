package com.fooddelivery.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequestDTO(
    @Email(message = "Email should be valid")
    String email,
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone format (E.164 format expected)")
    String phone,
    
    String firstName,
    String lastName
) {}

