package com.saksham.portal.submissions.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.saksham.portal.auth.util.JwtUtil;
import com.saksham.portal.submissions.dto.SubmissionEvaluationRequest;
import com.saksham.portal.submissions.dto.SubmissionResponse;
import com.saksham.portal.submissions.service.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final JwtUtil jwtUtil;

    // Student Endpoints
    @PostMapping
    public ResponseEntity<?> submitAssignment(
            @RequestParam("assignmentId") Long assignmentId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            SubmissionResponse response = submissionService.submitAssignment(userId, assignmentId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to submit assignment: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMySubmissions(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            List<SubmissionResponse> submissions = submissionService.getSubmissionsByUser(userId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get submissions: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubmissionById(@PathVariable Long id) {
        try {
            SubmissionResponse submission = submissionService.getSubmissionById(id);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Submission not found: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/evaluation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> evaluateSubmission(
            @PathVariable Long id,
            @Valid @RequestBody SubmissionEvaluationRequest request) {
        try {
            SubmissionResponse updatedSubmission = submissionService.updateSubmissionEvaluation(id, request);
            return ResponseEntity.ok(updatedSubmission);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update submission: " + e.getMessage());
        }
    }

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        try {
            List<SubmissionResponse> submissions = submissionService.getSubmissionsByAssignment(assignmentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get submissions: " + e.getMessage());
        }
    }
}