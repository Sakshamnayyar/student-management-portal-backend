package com.saksham.portal.submissions.dto;

import java.time.LocalDateTime;

public record AssignmentResponse(
    Long id,
    String title,
    String description,
    LocalDateTime dueDate,
    Long groupId,
    String groupName,
    Long createdById,
    String createdByUsername,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long submissionCount
) {
}