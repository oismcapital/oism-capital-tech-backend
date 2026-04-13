package com.oism.capitaltech.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
