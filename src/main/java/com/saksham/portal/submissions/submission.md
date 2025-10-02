# Assignment/Project Submission Implementation Plan

## Overview
This document outlines the implementation plan for the assignment/project submission functionality in the Student Management Portal.

## 1. Database Design

### Assignment Entity
- Store assignment details that administrators create for groups
- Fields: title, description, due date, group assignment, creation timestamp
- Relationship with Group and User (admin who created it)

### Submission Entity  
- Store student submissions for assignments
- Fields: assignment reference, student reference, file path, submission timestamp, status
- Relationship with Assignment and User (student who submitted)

### File Storage Strategy
- Store uploaded files on file system with organized directory structure
- Alternative: Cloud storage integration (AWS S3, Google Cloud Storage)
- File naming convention to avoid conflicts and ensure security

## 2. Package Structure

Following the existing project pattern:

```
com.saksham.portal.submissions/
├── controller/
│   ├── AssignmentController.java       # Admin assignment management
│   └── SubmissionController.java       # Student submission handling
├── dto/
│   ├── AssignmentRequest.java          # Create/update assignment payload
│   ├── AssignmentResponse.java         # Assignment details response
│   ├── SubmissionRequest.java          # File upload payload
│   └── SubmissionResponse.java         # Submission details response
├── model/
│   ├── Assignment.java                 # Assignment JPA entity
│   └── Submission.java                 # Submission JPA entity
├── repository/
│   ├── AssignmentRepository.java       # Assignment data access
│   └── SubmissionRepository.java       # Submission data access
└── service/
    ├── AssignmentService.java          # Assignment business logic
    └── SubmissionService.java          # Submission business logic
```

## 3. Key Features to Implement

### Admin Features
- **Create Assignments**: Admins can create assignments for specific groups
- **View All Submissions**: Access to all submissions for any assignment
- **Download Files**: Download submitted assignment files
- **Manage Assignments**: Update assignment details, extend due dates
- **Grade Submissions**: Optional feature to assign grades and feedback

### Student Features  
- **View Assignments**: See assignments assigned to their group
- **Upload Files**: Submit assignment files with validation
- **Submission History**: View their past submissions and status
- **Download Assignments**: Download assignment description files (if provided)

## 4. API Endpoints Plan

### Assignment Management Endpoints

#### Admin Endpoints
```
POST   /api/assignments                 # Create new assignment
GET    /api/assignments/all             # Get all assignments (admin view)
PUT    /api/assignments/{id}            # Update assignment details
DELETE /api/assignments/{id}            # Delete assignment
GET    /api/assignments/{id}/submissions # Get all submissions for assignment
```

#### Student Endpoints
```
GET    /api/assignments                 # Get assignments for user's group
GET    /api/assignments/{id}            # Get specific assignment details
```

### Submission Management Endpoints

#### Student Endpoints
```
POST   /api/submissions                 # Submit assignment file
GET    /api/submissions/my              # Get user's submission history
GET    /api/submissions/{id}            # Get specific submission details
PUT    /api/submissions/{id}            # Update submission (if allowed)
```

#### Admin Endpoints
```
GET    /api/submissions/assignment/{assignmentId}  # Get all submissions for assignment
GET    /api/submissions/{id}/download              # Download submission file
POST   /api/submissions/{id}/grade                 # Grade submission (optional)
```

#### File Management
```
GET    /api/files/submissions/{filename}           # Secure file download
GET    /api/files/assignments/{filename}           # Download assignment files
```

## 5. Technical Implementation Details

### File Upload Handling
- Use Spring Boot's `MultipartFile` for file uploads
- Implement file size validation (e.g., max 50MB)
- Validate file types (PDF, DOC, DOCX, ZIP, etc.)
- Generate unique filenames to prevent conflicts
- Organize files in directory structure: `/uploads/assignments/{assignmentId}/{userId}/`

### Security Considerations
- Validate file types and extensions
- Scan for malicious content
- Implement proper access controls for file downloads
- Ensure only authorized users can access files
- Use secure file paths to prevent directory traversal attacks

### Database Schema

#### Assignment Table
```sql
CREATE TABLE assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    group_id BIGINT,
    created_by BIGINT,
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);
```

#### Submission Table
```sql
CREATE TABLE submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    file_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('SUBMITTED', 'GRADED', 'LATE') DEFAULT 'SUBMITTED',
    grade DECIMAL(5,2),
    feedback TEXT,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_user_assignment (assignment_id, user_id)
);
```

## 6. Database Relationships

### Entity Relationships
- **Assignment** belongs to **Group** (many-to-one)
- **Assignment** created by **User** (Admin) (many-to-one)  
- **Submission** belongs to **Assignment** (many-to-one)
- **Submission** belongs to **User** (Student) (many-to-one)
- One student can have only one submission per assignment (enforced by unique constraint)

### JPA Annotations
```java
// In Assignment entity
@ManyToOne
@JoinColumn(name = "group_id")
private Group group;

@ManyToOne  
@JoinColumn(name = "created_by")
private User createdBy;

@OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
private List<Submission> submissions;

// In Submission entity
@ManyToOne
@JoinColumn(name = "assignment_id")
private Assignment assignment;

@ManyToOne
@JoinColumn(name = "user_id") 
private User user;
```

## 7. Implementation Order

### Phase 1: Core Models and Repositories
1. Create Assignment and Submission JPA entities
2. Implement AssignmentRepository and SubmissionRepository
3. Add database migrations/schema updates

### Phase 2: Service Layer
1. Implement AssignmentService with CRUD operations
2. Implement SubmissionService with file handling
3. Add validation and business logic

### Phase 3: Controller Layer  
1. Create AssignmentController with admin endpoints
2. Create SubmissionController with student endpoints
3. Implement file upload/download endpoints

### Phase 4: Security and Validation
1. Add role-based access control
2. Implement file validation and security

## 8. Configuration Requirements

### Application Properties
```properties
# File upload settings
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# File storage location  
app.file.upload-dir=uploads/
app.file.allowed-types=pdf,doc,docx,zip,rar,txt,jpg,png
```s

### File Storage Configuration
- Create uploads directory structure
- Set proper file permissions
- Configure backup strategy for uploaded files
- Implement file cleanup for old submissions

## 9. Future Enhancements

## Notes for Implementation

DONT ASSUME ANYTHING and DONT OVERENGINEER ANYTHING.

- Follow existing code patterns and naming conventions
- Ensure consistent error handling across all endpoints
- Use existing authentication and authorization mechanisms
- Maintain backward compatibility with current API structure
- Document all new endpoints with proper API documentation
- Consider performance implications for file operations
- Implement proper logging for audit trails