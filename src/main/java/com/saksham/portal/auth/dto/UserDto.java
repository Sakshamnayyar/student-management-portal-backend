package com.saksham.portal.auth.dto;

import com.saksham.portal.common.enums.Role;
import com.saksham.portal.common.enums.UserStatus;
import com.saksham.portal.users.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private UserStatus status;
    private String groupName;
    private Long groupId;
    
    public static UserDto fromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .groupName(user.getGroup() != null ? user.getGroup().getName() : null)
                .groupId(user.getGroup() != null ? user.getGroup().getId() : null)
                .build();
    }
}