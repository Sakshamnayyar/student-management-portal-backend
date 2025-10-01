package com.saksham.portal.users.dto;

import com.saksham.portal.common.enums.Role;
import com.saksham.portal.common.enums.UserStatus;

public record UpdateUserRequest(
    String username,
    String email,
    Role role,
    UserStatus status
) {}
