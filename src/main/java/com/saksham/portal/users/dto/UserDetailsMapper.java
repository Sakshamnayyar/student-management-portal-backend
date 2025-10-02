package com.saksham.portal.users.dto;

import org.springframework.stereotype.Component;

import com.saksham.portal.users.model.User;
import com.saksham.portal.users.model.UserDetails;

@Component
public class UserDetailsMapper {

    //Convert UserDetails entity to UserDetailsResponse DTO
    public UserDetailsResponse toResponse(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }

        return new UserDetailsResponse(
            userDetails.getId(),
            userDetails.getUser().getId(),
            userDetails.getUser().getUsername(),
            userDetails.getUser().getEmail(),
            userDetails.getFirstName(),
            userDetails.getLastName(),
            userDetails.getReferralSource(),
            userDetails.getDateOfBirth(),
            userDetails.getEadType(),
            userDetails.getEadStartDate(),
            userDetails.getPriorExperience(),
            userDetails.getExperienceDetails(),
            userDetails.getProgrammingLanguages(),
            buildFileUrl(userDetails.getResumeFilePath()),
            buildFileUrl(userDetails.getEadFilePath()),
            buildFileUrl(userDetails.getIdFilePath()),
            userDetails.getProfileCompleted(),
            calculateCompletionPercentage(userDetails),
            userDetails.getCreatedAt(),
            userDetails.getUpdatedAt()
        );
    }

    //Convert UserDetailsRequest DTO to UserDetails entity
    public UserDetails toEntity(UserDetailsRequest request, User user) {
        if (request == null || user == null) {
            return null;
        }

        return UserDetails.builder()
            .user(user)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .referralSource(request.getReferralSource())
            .dateOfBirth(request.getDateOfBirth())
            .eadType(request.getEadType())
            .eadStartDate(request.getEadStartDate())
            .priorExperience(request.getPriorExperience() != null ? request.getPriorExperience() : false)
            .experienceDetails(request.getExperienceDetails())
            .programmingLanguages(request.getProgrammingLanguages())
            .build();
    }

    //Update existing UserDetails entity with data from request
    public void updateEntity(UserDetails userDetails, UserDetailsRequest request) {
        if (userDetails == null || request == null) {
            return;
        }

        if (request.getFirstName() != null) {
            userDetails.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            userDetails.setLastName(request.getLastName());
        }
        if (request.getReferralSource() != null) {
            userDetails.setReferralSource(request.getReferralSource());
        }
        if (request.getDateOfBirth() != null) {
            userDetails.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getEadType() != null) {
            userDetails.setEadType(request.getEadType());
        }
        if (request.getEadStartDate() != null) {
            userDetails.setEadStartDate(request.getEadStartDate());
        }
        if (request.getPriorExperience() != null) {
            userDetails.setPriorExperience(request.getPriorExperience());
        }
        if (request.getExperienceDetails() != null) {
            userDetails.setExperienceDetails(request.getExperienceDetails());
        }
        if (request.getProgrammingLanguages() != null) {
            userDetails.setProgrammingLanguages(request.getProgrammingLanguages());
        }
    }

    //Calculate profile completion percentage
    public int calculateCompletionPercentage(UserDetails userDetails) {
        if (userDetails == null) {
            return 0;
        }

        int totalFields = 9; 
        int filledFields = 0;

        if (isNotEmpty(userDetails.getFirstName())) filledFields++;
        if (isNotEmpty(userDetails.getLastName())) filledFields++;
        if (isNotEmpty(userDetails.getReferralSource())) filledFields++;
        if (userDetails.getDateOfBirth() != null) filledFields++;
        if (isNotEmpty(userDetails.getEadType())) filledFields++;
        if (userDetails.getEadStartDate() != null) filledFields++;
        if (isNotEmpty(userDetails.getResumeFilePath())) filledFields++;
        if (isNotEmpty(userDetails.getEadFilePath())) filledFields++;
        if (isNotEmpty(userDetails.getIdFilePath())) filledFields++;

        return (int) Math.round((filledFields * 100.0) / totalFields);
    }

    private String buildFileUrl(String filePath) {
        return filePath;
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
