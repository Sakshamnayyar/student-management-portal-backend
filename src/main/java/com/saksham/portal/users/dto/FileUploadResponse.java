package com.saksham.portal.users.dto;

import java.time.LocalDateTime;

public record FileUploadResponse(
    String fileName,
    String fileUrl,
    Long fileSize,
    String documentType,
    LocalDateTime uploadedAt
) {}
