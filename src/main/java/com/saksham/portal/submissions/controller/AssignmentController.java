package com.saksham.portal.submissions.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saksham.portal.auth.util.JwtUtil;
import com.saksham.portal.submissions.dto.AssignmentRequest;
import com.saksham.portal.submissions.dto.AssignmentResponse;
import com.saksham.portal.submissions.dto.SubmissionResponse;
import com.saksham.portal.submissions.service.AssignmentService;
import com.saksham.portal.submissions.service.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final JwtUtil jwtUtil;

    // Admin Endpoints
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAssignment(
            @Valid @RequestBody AssignmentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long createdById = jwtUtil.extractUserId(token);
            AssignmentResponse response = assignmentService.createAssignment(createdById, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create assignment: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAssignments() {
        try {
            List<AssignmentResponse> assignments = assignmentService.getAllAssignments();
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get assignments: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest request) {
        try {
            AssignmentResponse response = assignmentService.updateAssignment(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update assignment: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.ok("Assignment deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete assignment: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAssignmentSubmissions(@PathVariable Long id) {
        try {
            List<SubmissionResponse> submissions = submissionService.getSubmissionsByAssignment(id);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get submissions: " + e.getMessage());
        }
    }

    // Student Endpoints
    @GetMapping
    public ResponseEntity<?> getAssignmentsForUser(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            // Get user's group and fetch assignments for that group
            // This will be handled by the service layer
            List<AssignmentResponse> assignments = assignmentService.getAssignmentsByUserGroup(userId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get assignments: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long id) {
        try {
            AssignmentResponse assignment = assignmentService.getAssignmentById(id);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Assignment not found: " + e.getMessage());
        }
    }
}