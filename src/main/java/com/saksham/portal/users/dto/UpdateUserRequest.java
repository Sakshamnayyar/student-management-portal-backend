package com.saksham.portal.users.dto;

import com.saksham.portal.common.eums.Role;
import com.saksham.portal.common.eums.UserStatus;

public record UpdateUserRequest(
    String username,
    String email,
    Role role,
    UserStatus status
) {}
