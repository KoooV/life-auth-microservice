package com.kov.lifeauthmicroservice.DTO;

import com.kov.lifeauthmicroservice.model.Role;

import java.util.UUID;

public record AuthResponse(String accessToken, String refreshToken, UUID userId, String username, String email, Role role) {
}
