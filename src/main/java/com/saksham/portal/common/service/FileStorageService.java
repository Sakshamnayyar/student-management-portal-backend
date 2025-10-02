package com.saksham.portal.common.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.max-size}")
    private long maxFileSize;

    public String uploadFile(MultipartFile file, String fileType, Long userId) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Cannot upload empty file");
        }

        if (!validateFile(file, fileType)) {
            throw new IOException("Invalid file type or size");
        }

        // Create directory structure: uploads/user-profiles/{userId}/{fileType}/
        String directoryPath = uploadDir + "user-profiles/" + userId + "/" + fileType + "/";
        Path directory = Paths.get(directoryPath);
        
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Full file path
        Path filePath = directory.resolve(uniqueFilename);
        
        // Copy file to destination
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for database storage
        return directoryPath + uniqueFilename;
    }

    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    public Resource loadFileAsResource(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        Resource resource = new UrlResource(path.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("Could not read file: " + filePath);
        }
    }

    public boolean validateFile(MultipartFile file, String fileType) {
        if (file.isEmpty()) {
            return false;
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            return false;
        }

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        // Validate based on file type
        return switch (fileType) {
            case "resume" -> extension.matches("pdf|doc|docx");
            case "ead", "id" -> extension.matches("pdf|jpg|jpeg|png");
            default -> false;
        };
    }
}
