package com.saksham.portal.auth.dto;

import com.saksham.portal.users.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private UserDto user;
    
    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = UserDto.fromUser(user);
    }
}
