package com.saksham.portal.users.dto;

import java.time.LocalDateTime;

import com.saksham.portal.common.eums.Role;
import com.saksham.portal.common.eums.UserStatus;

public record UserResponse(
    Long id,
    String username,
    String email,
    Role role,
    UserStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
