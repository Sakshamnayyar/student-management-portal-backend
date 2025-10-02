package com.saksham.portal.submissions.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileUploadProperties {
    
    private String uploadDir;
    private Long maxSize;
    private String allowedTypes;
}