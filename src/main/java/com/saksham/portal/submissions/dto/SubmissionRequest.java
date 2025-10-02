package com.saksham.portal.submissions.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionRequest {
    
    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;
}