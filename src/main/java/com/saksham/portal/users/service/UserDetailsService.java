package com.saksham.portal.users.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.saksham.portal.common.service.FileStorageService;
import com.saksham.portal.users.dto.UserDetailsRequest;
import com.saksham.portal.users.dto.UserDetailsResponse;
import com.saksham.portal.users.model.User;
import com.saksham.portal.users.model.UserDetails;
import com.saksham.portal.users.repository.UserDetailsRepository;
import com.saksham.portal.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsService {

    private final UserDetailsRepository userDetailsRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public UserDetailsResponse createUserDetails(Long userId, UserDetailsRequest request) {
        // Check if profile already exists
        if (userDetailsRepository.existsByUserId(userId)) {
            throw new RuntimeException("User profile already exists");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create user details
        UserDetails userDetails = UserDetails.builder()
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

        userDetails = userDetailsRepository.save(userDetails);

        return mapToResponse(userDetails);
    }

    @Transactional
    public UserDetailsResponse updateUserDetails(Long userId, UserDetailsRequest request) {
        UserDetails userDetails = userDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        // Update fields
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

        // Check profile completion
        int completionPercentage = calculateProfileCompletionPercentage(userId);
        userDetails.setProfileCompleted(completionPercentage == 100);

        userDetails = userDetailsRepository.save(userDetails);

        return mapToResponse(userDetails);
    }

    public UserDetailsResponse getUserDetailsByUserId(Long userId) {
        UserDetails userDetails = userDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        return mapToResponse(userDetails);
    }

    public UserDetailsResponse getUserDetailsByUsername(String username) {
        UserDetails userDetails = userDetailsRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        return mapToResponse(userDetails);
    }

    @Transactional
    public void uploadProfileDocument(Long userId, MultipartFile file, String documentType) {
        UserDetails userDetails = userDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        try {
            // Delete old file if exists
            String oldFilePath = getOldFilePath(userDetails, documentType);
            if (oldFilePath != null) {
                fileStorageService.deleteFile(oldFilePath);
            }

            // Upload new file
            String newFilePath = fileStorageService.uploadFile(file, documentType, userId);

            // Update file path in database
            updateFilePath(userDetails, documentType, newFilePath);

            // Check profile completion
            int completionPercentage = calculateProfileCompletionPercentage(userId);
            userDetails.setProfileCompleted(completionPercentage == 100);

            userDetailsRepository.save(userDetails);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteProfileDocument(Long userId, String documentType) {
        UserDetails userDetails = userDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        try {
            String filePath = getOldFilePath(userDetails, documentType);
            if (filePath != null) {
                fileStorageService.deleteFile(filePath);
                updateFilePath(userDetails, documentType, null);
                
                // Update profile completion
                int completionPercentage = calculateProfileCompletionPercentage(userId);
                userDetails.setProfileCompleted(completionPercentage == 100);
                
                userDetailsRepository.save(userDetails);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

    public int calculateProfileCompletionPercentage(Long userId) {
        UserDetails userDetails = userDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        int totalFields = 9; // firstName, lastName, referralSource, dateOfBirth, eadType, eadStartDate, resume, ead, id
        int filledFields = 0;

        if (userDetails.getFirstName() != null && !userDetails.getFirstName().isEmpty()) filledFields++;
        if (userDetails.getLastName() != null && !userDetails.getLastName().isEmpty()) filledFields++;
        if (userDetails.getReferralSource() != null && !userDetails.getReferralSource().isEmpty()) filledFields++;
        if (userDetails.getDateOfBirth() != null) filledFields++;
        if (userDetails.getEadType() != null && !userDetails.getEadType().isEmpty()) filledFields++;
        if (userDetails.getEadStartDate() != null) filledFields++;
        if (userDetails.getResumeFilePath() != null && !userDetails.getResumeFilePath().isEmpty()) filledFields++;
        if (userDetails.getEadFilePath() != null && !userDetails.getEadFilePath().isEmpty()) filledFields++;
        if (userDetails.getIdFilePath() != null && !userDetails.getIdFilePath().isEmpty()) filledFields++;

        return (int) Math.round((filledFields * 100.0) / totalFields);
    }

    // Helper methods
    
    private UserDetailsResponse mapToResponse(UserDetails userDetails) {
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
            calculateProfileCompletionPercentage(userDetails.getUser().getId()),
            userDetails.getCreatedAt(),
            userDetails.getUpdatedAt()
        );
    }

    private String buildFileUrl(String filePath) {
        return filePath;
    }

    private String getOldFilePath(UserDetails userDetails, String documentType) {
        return switch (documentType) {
            case "resume" -> userDetails.getResumeFilePath();
            case "ead" -> userDetails.getEadFilePath();
            case "id" -> userDetails.getIdFilePath();
            default -> null;
        };
    }

    private void updateFilePath(UserDetails userDetails, String documentType, String newFilePath) {
        switch (documentType) {
            case "resume" -> userDetails.setResumeFilePath(newFilePath);
            case "ead" -> userDetails.setEadFilePath(newFilePath);
            case "id" -> userDetails.setIdFilePath(newFilePath);
        }
    }
}
