package com.kov.lifeauthmicroservice.DTO;

import com.kov.lifeauthmicroservice.model.Role;
import java.util.UUID;

public record UserServiceDTO(
    UUID id,
    String username,
    String email,
    Role role,
    boolean enabled,
    boolean locked
) {}
