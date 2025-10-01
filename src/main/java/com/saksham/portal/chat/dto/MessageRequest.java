package com.saksham.portal.chat.dto;

public record MessageRequest (
    Long receiverId,
    Long groupId,
    String content
) {
}