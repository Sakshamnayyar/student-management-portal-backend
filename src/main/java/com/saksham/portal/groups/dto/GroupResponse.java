package com.saksham.portal.groups.dto;

import java.util.List;

import com.saksham.portal.groups.model.Group;
import com.saksham.portal.users.model.User;


public record GroupResponse (
    Long id,
    String name,
    String description,
    List<Long> userIds
){
    public static GroupResponse fromEntity(Group group) {
        return new GroupResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            group.getUsers() != null ? 
                group.getUsers().stream().map(User::getId).toList() : 
                List.of()
        );
    }
    
}
