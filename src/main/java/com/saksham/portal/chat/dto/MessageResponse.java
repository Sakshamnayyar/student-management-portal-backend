package com.saksham.portal.chat.dto;

import java.time.LocalDateTime;

import com.saksham.portal.groups.model.Group;
import com.saksham.portal.users.model.User;

public record MessageResponse(
    Long id,
    Long senderId,
    Long receiverId,
    Long groupId,
    String content,
    LocalDateTime timestamp
) { 
}
