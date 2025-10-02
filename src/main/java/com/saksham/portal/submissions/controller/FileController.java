package com.saksham.portal.submissions.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saksham.portal.auth.util.JwtUtil;
import com.saksham.portal.submissions.dto.SubmissionResponse;
import com.saksham.portal.submissions.service.SubmissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final SubmissionService submissionService;
    private final JwtUtil jwtUtil;

    @GetMapping("/submissions/{submissionId}/download")
    public ResponseEntity<?> downloadSubmissionFile(
            @PathVariable Long submissionId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            // Get submission details
            SubmissionResponse submission = submissionService.getSubmissionById(submissionId);
            
            // Check if user has permission to download
            // Either the user owns the submission or is an admin
            String userRole = jwtUtil.extractRole(token);
            if (!submission.userId().equals(userId) && !"ADMIN".equals(userRole)) {
                return ResponseEntity.status(403).body("Access denied");
            }
            
            // Get file path
            String filePath = submissionService.getFilePath(submissionId);
            Path path = Paths.get(filePath);
            
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(path);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + submission.fileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to download file: " + e.getMessage());
        }
    }
}