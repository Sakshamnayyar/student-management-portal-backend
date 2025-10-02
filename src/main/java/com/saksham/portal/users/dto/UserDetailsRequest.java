package com.saksham.portal.users.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsRequest {
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;
    
    @Size(max = 500, message = "Referral source cannot exceed 500 characters")
    private String referralSource;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Size(max = 100, message = "EAD type cannot exceed 100 characters")
    private String eadType;
    
    private LocalDate eadStartDate;
    
    private Boolean priorExperience;
    
    @Size(max = 5000, message = "Experience details cannot exceed 5000 characters")
    private String experienceDetails;
    
    @Size(max = 1000, message = "Programming languages cannot exceed 1000 characters")
    private String programmingLanguages;
}
