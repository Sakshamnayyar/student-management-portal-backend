package com.saksham.portal.users.dto;

public record BasicUserInfoResponse(
    Long id,
    String username,
    String email
) {}
