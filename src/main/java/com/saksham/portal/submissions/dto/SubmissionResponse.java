package com.saksham.portal.submissions.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.saksham.portal.common.enums.SubmissionStatus;

public record SubmissionResponse(
    Long id,
    Long assignmentId,
    String assignmentTitle,
    Long userId,
    String username,
    String fileName,
    Long fileSize,
    SubmissionStatus status,
    BigDecimal grade,
    String feedback,
    LocalDateTime submittedAt
) {
}