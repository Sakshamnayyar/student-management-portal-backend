package com.saksham.portal.submissions.dto;

import java.math.BigDecimal;

import com.saksham.portal.common.enums.SubmissionStatus;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmissionEvaluationRequest(
    @NotNull(message = "Status is required")
    SubmissionStatus status,

    @DecimalMin(value = "0.0", inclusive = true, message = "Grade must be greater than or equal to 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Grade must be less than or equal to 100")
    BigDecimal grade,

    @Size(max = 2000, message = "Feedback must not exceed 2000 characters")
    String feedback
) {}
