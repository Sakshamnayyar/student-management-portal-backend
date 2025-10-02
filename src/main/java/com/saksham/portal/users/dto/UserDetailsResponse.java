package com.saksham.portal.users.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserDetailsResponse(
    Long id,
    Long userId,
    String username,
    String email,
    String firstName,
    String lastName,
    String referralSource,
    LocalDate dateOfBirth,
    String eadType,
    LocalDate eadStartDate,
    Boolean priorExperience,
    String experienceDetails,
    String programmingLanguages,
    String resumeFileUrl,
    String eadFileUrl,
    String idFileUrl,
    Boolean profileCompleted,
    Integer completionPercentage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
