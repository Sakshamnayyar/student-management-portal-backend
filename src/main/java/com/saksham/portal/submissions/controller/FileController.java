package com.saksham.portal.submissions.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.saksham.portal.auth.util.JwtUtil;
import com.saksham.portal.common.service.FileStorageService;
import com.saksham.portal.submissions.dto.SubmissionResponse;
import com.saksham.portal.submissions.service.SubmissionService;
import com.saksham.portal.users.dto.FileUploadResponse;
import com.saksham.portal.users.service.UserDetailsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final SubmissionService submissionService;
    private final UserDetailsService userDetailsService;
    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    @GetMapping("/submissions/{submissionId}/download")
    public ResponseEntity<?> downloadSubmissionFile(
            @PathVariable Long submissionId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            SubmissionResponse submission = submissionService.getSubmissionById(submissionId);
            
            String userRole = jwtUtil.extractRole(token);
            if (!submission.userId().equals(userId) && !"ADMIN".equals(userRole)) {
                return ResponseEntity.status(403).body("Access denied");
            }
            
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

    // Profile document endpoints
    @PostMapping("/profile/upload/{documentType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadProfileDocument(
            @PathVariable String documentType,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            if (!documentType.matches("resume|ead|id")) {
                return ResponseEntity.badRequest().body("Invalid document type. Must be: resume, ead, or id");
            }

            userDetailsService.uploadProfileDocument(userId, file, documentType);

            return ResponseEntity.ok(new FileUploadResponse(
                file.getOriginalFilename(),
                "/api/files/profile/download/" + documentType,
                file.getSize(),
                documentType,
                LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
        }
    }

    @GetMapping("/profile/download/{documentType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> downloadProfileDocument(
            @PathVariable String documentType,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            // Get user details to find file path
            var userDetails = userDetailsService.getUserDetailsByUserId(userId);
            
            String filePath = switch (documentType) {
                case "resume" -> userDetails.resumeFileUrl();
                case "ead" -> userDetails.eadFileUrl();
                case "id" -> userDetails.idFileUrl();
                default -> null;
            };

            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileStorageService.loadFileAsResource(filePath);
            
            String contentType = Files.probeContentType(Paths.get(filePath));
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + Paths.get(filePath).getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to download file: " + e.getMessage());
        }
    }

    @GetMapping("/profile/{userId}/download/{documentType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> downloadUserProfileDocument(
            @PathVariable Long userId,
            @PathVariable String documentType) {
        try {
            var userDetails = userDetailsService.getUserDetailsByUserId(userId);
            
            String filePath = switch (documentType) {
                case "resume" -> userDetails.resumeFileUrl();
                case "ead" -> userDetails.eadFileUrl();
                case "id" -> userDetails.idFileUrl();
                default -> null;
            };

            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileStorageService.loadFileAsResource(filePath);
            
            String contentType = Files.probeContentType(Paths.get(filePath));
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + Paths.get(filePath).getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to download file: " + e.getMessage());
        }
    }

    @DeleteMapping("/profile/{documentType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteProfileDocument(
            @PathVariable String documentType,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            if (!documentType.matches("resume|ead|id")) {
                return ResponseEntity.badRequest().body("Invalid document type. Must be: resume, ead, or id");
            }

            userDetailsService.deleteProfileDocument(userId, documentType);
            return ResponseEntity.ok("Document deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete file: " + e.getMessage());
        }
    }
}