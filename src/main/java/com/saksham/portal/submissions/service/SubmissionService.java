package com.saksham.portal.submissions.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.saksham.portal.common.enums.Role;
import com.saksham.portal.common.enums.SubmissionStatus;
import com.saksham.portal.common.service.NotificationEmailService;
import com.saksham.portal.submissions.config.FileUploadProperties;
import com.saksham.portal.submissions.dto.SubmissionEvaluationRequest;
import com.saksham.portal.submissions.dto.SubmissionResponse;
import com.saksham.portal.submissions.model.Assignment;
import com.saksham.portal.submissions.model.Submission;
import com.saksham.portal.submissions.repository.AssignmentRepository;
import com.saksham.portal.submissions.repository.SubmissionRepository;
import com.saksham.portal.users.model.User;
import com.saksham.portal.users.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final FileUploadProperties fileUploadProperties;
    private final NotificationEmailService notificationEmailService;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(fileUploadProperties.getUploadDir()));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Transactional
    public SubmissionResponse submitAssignment(Long userId, Long assignmentId, MultipartFile file) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (submissionRepository.existsByAssignmentIdAndUserId(assignmentId, userId)) {
            throw new RuntimeException("You have already submitted for this assignment");
        }

        validateFile(file);

        SubmissionStatus status = SubmissionStatus.PENDING;
        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            // Note: add LATE status
            status = SubmissionStatus.PENDING; 
        }

        String fileName = saveFile(file, assignmentId, userId);

        Submission submission = Submission.builder()
                .assignment(assignment)
                .user(user)
                .fileName(file.getOriginalFilename())
                .filePath(fileName)
                .fileSize(file.getSize())
                .status(status)
                .build();

        Submission saved = submissionRepository.save(submission);
        
        // Send email notification to all admins about new submission
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notificationEmailService.sendSubmissionNotification(
                admin.getEmail(),
                assignment.getTitle(),
                user.getUsername()
            );
        }
        
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByUser(Long userId) {
        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        return toResponse(submission);
    }

    @Transactional
    public SubmissionResponse updateSubmissionEvaluation(Long submissionId, SubmissionEvaluationRequest request) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        submission.setStatus(request.status());
        submission.setGrade(request.grade());
        submission.setFeedback(request.feedback());

        Submission updated = submissionRepository.save(submission);
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public String getFilePath(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        return Paths.get(fileUploadProperties.getUploadDir(), submission.getFilePath()).toString();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > fileUploadProperties.getMaxSize()) {
            throw new RuntimeException("File size exceeds maximum allowed size of " + (fileUploadProperties.getMaxSize() / 1024 / 1024) + " MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("File name is invalid");
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!isAllowedFileType(fileExtension)) {
            throw new RuntimeException("File type not allowed. Allowed types: " + fileUploadProperties.getAllowedTypes());
        }
    }

    private String saveFile(MultipartFile file, Long assignmentId, Long userId) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            // Create directory: uploads/assignments/{assignmentId}/{userId}/
            Path assignmentDir = Paths.get(fileUploadProperties.getUploadDir(), "assignments", assignmentId.toString(), userId.toString());
            Files.createDirectories(assignmentDir);
            
            Path filePath = assignmentDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return Paths.get("assignments", assignmentId.toString(), userId.toString(), uniqueFileName).toString();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    private boolean isAllowedFileType(String extension) {
        String[] allowedExtensions = fileUploadProperties.getAllowedTypes().split(",");
        for (String allowed : allowedExtensions) {
            if (allowed.trim().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private SubmissionResponse toResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getAssignment().getTitle(),
                submission.getUser().getId(),
                submission.getUser().getUsername(),
                submission.getFileName(),
                submission.getFileSize(),
                submission.getStatus(),
                submission.getGrade(),
                submission.getFeedback(),
                submission.getSubmittedAt()
        );
    }
}