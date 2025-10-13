**Documentation – Student Management Portal**
- Overview: Java 21 Spring Boot backend for managing student onboarding, communication, assignments, and submissions with JWT-secured REST APIs backed by MySQL.
- Table of Contents
  - Project Overview
  - Architecture
  - Folder Structure
  - Setup & Local Development
  - Authentication & Security
  - Modules
    - Auth
    - Users & Profiles
    - Groups
    - Chat
    - Submissions
  - Notifications & Emails
  - Database Schema
  - Coding Conventions & Tips

**Project Overview**
- What: REST backend for the student portal (registration, user management, cohort grouping, messaging, assignments).
- Why: Streamlines onboarding and tracking for admins while providing students a single place for communication and coursework.
- How: Spring Boot services organized per domain module, MySQL persistence via Spring Data JPA, JWT auth, email alerts.

**Architecture**
- Layered per module: Controller (REST) → Service (business logic) → Repository (JPA) → Entity (Persistence).
- Cross-cutting components: security (`auth.config`), notifications (`common.service`), utilities (JWT, file storage).
- Method-level access control with `@PreAuthorize`, JWT filter ensures stateless requests.

**Folder Structure**
```
src/main/java/com/saksham/portal
├─ PortalApplication.java
├─ auth/ (JWT auth, security config)
├─ users/ (user core + profile details)
├─ groups/ (cohort management)
├─ chat/ (direct/group messaging)
├─ submissions/ (assignments, uploads)
└─ common/ (enums, email + file helpers)
```
Resources: `src/main/resources/application.properties` for DB, JWT, mail, upload settings. Local uploads land under workspace `uploads/`.

**Setup & Local Development**
- Prerequisites:
  - `Java 21`
  - `MySQL 8` (create `student_portal` schema)
  - Maven (use repo’s `mvnw` wrappers)
- Environment config: copy `application.properties` or externalize via `.env`. Key properties:
  ```
  spring.datasource.url=jdbc:mysql://localhost:3306/student_portal
  spring.datasource.username=...
  spring.datasource.password=...
  jwt.secret=<64+ chars>
  jwt.expiration=86400000
  app.frontend.url=http://localhost:4000
  app.file.upload-dir=uploads/
  spring.mail.username=n00replyportal@gmail.com
  spring.mail.password=<app specific password>
  ```
  For prod use environment variables or secrets and disable `spring.jpa.hibernate.ddl-auto=update`.
- Run locally:
  1. `./mvnw clean install`
  2. `./mvnw spring-boot:run`
  3. API base URL defaults to `http://localhost:3000`.
- Tests: `./mvnw test`.
- MySQL migrations: schema auto-managed via JPA (`ddl-auto=update`); swap to Flyway for controlled DDL when ready.

**Authentication & Security**
- `SecurityConfig`:
  - CORS restricted to `${app.frontend.url}`.
  - Stateless JWT auth; `/api/auth/**`, `/health/**`, `/error` are public.
  - All other routes require `Authorization: Bearer <token>`.
- `JwtAuthenticationFilter` extracts user ID & role, populates `SecurityContext`.
- `JwtUtil` handles token generation/validation using HS256 and `jwt.secret`.
- Passwords hashed with `BCryptPasswordEncoder`.
- Role-based guards: `@PreAuthorize("hasRole('ADMIN')")`, `hasAnyRole('ADMIN','USER')`.
- Roles (`Role` enum): `ADMIN`, `USER`.
- User lifecycle statuses (`UserStatus`): `ONBOARDING`, `TRAINING`, `MARKETING`, `SUSPENDED`.

**Modules**

***Auth***
- Purpose: Register/login users, issue JWT tokens.
- Key classes: `AuthController`, `AuthService`, `JwtUtil`, DTOs (`LoginRequest`, `RegisterRequest`, `AuthResponse`).
- Endpoints:

| Method | Path             | Description            | Security |
|--------|------------------|------------------------|----------|
| POST   | `/api/auth/register` | Create standard user | Public   |
| POST   | `/api/auth/login`    | Authenticate & JWT   | Public   |

- Sample register request:
  ```json
  {
    "username": "jdoe",
    "email": "jdoe@example.com",
    "password": "SecurePass123!"
  }
  ```
- Sample login response:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR...",
    "user": {
      "id": 7,
      "username": "jdoe",
      "email": "jdoe@example.com",
      "role": "USER",
      "status": "ONBOARDING",
      "groupName": null,
      "groupId": null
    }
  }
  ```

***Users & Profiles***
- Purpose: Admin user management and student self-service profile.
- Key classes: `UserController`, `UserDetailsController`, `UserService`, `UserDetailsService`, repositories, DTOs (`UserResponse`, `UpdateUserRequest`, `UserDetails*`).
- Endpoints:

| Method | Path | Description | Security |
|--------|------|-------------|----------|
| GET | `/api/users` | List all students (Role.USER) | Admin |
| GET | `/api/users/{id}` | Detailed user | Admin |
| GET | `/api/users/{id}/basic` | Basic info | Admin/User |
| PUT | `/api/users/{id}` | Update user role/status | Admin |
| POST | `/api/user-details/me` | Create personal profile | Authenticated |
| GET | `/api/user-details/me` | Get own profile | Authenticated |
| PUT | `/api/user-details/me` | Update own profile | Authenticated |
| GET | `/api/user-details/me/completion` | Completion percent | Authenticated |
| GET | `/api/user-details/{userId}` | Admin fetch profile | Admin |
| PUT | `/api/user-details/{userId}` | Admin update profile | Admin |

- Sample update user request:
  ```json
  {
    "email": "new.email@example.com",
    "role": "ADMIN",
    "status": "TRAINING"
  }
  ```
- Sample profile response (trimmed):
  ```json
  {
    "id": 12,
    "userId": 7,
    "firstName": "Jane",
    "lastName": "Doe",
    "priorExperience": true,
    "programmingLanguages": "Java, React",
    "resumeFileUrl": "uploads/user-profiles/7/resume/UUID.pdf",
    "completionPercentage": 78
  }
  ```

***Groups***
- Purpose: Cohort assignments for coordination and content targeting.
- Key classes: `GroupController`, `GroupService`, `GroupRepository`, DTOs (`CreateGroupRequest`, `GroupResponse`).
- Endpoints:

| Method | Path | Description | Security |
|--------|------|-------------|----------|
| POST | `/api/groups` | Create group (optional user assignments) | Admin |
| GET | `/api/groups` | List groups | Admin |
| PUT | `/api/groups/{groupId}` | Update info & membership | Admin |
| DELETE | `/api/groups/{groupId}` | Remove group | Admin |
| PUT | `/api/groups/{groupId}/users/{userId}` | Assign user | Admin |
| DELETE | `/api/groups/{groupId}/users/{userId}` | Remove user | Admin |
| GET | `/api/groups/{groupId}/users` | List members | Admin/User (token required) |

- Sample create request:
  ```json
  {
    "name": "Cohort Alpha",
    "description": "Java May 2025",
    "userIds": [7, 9, 10]
  }
  ```
- Response snippet:
  ```json
  {
    "id": 3,
    "name": "Cohort Alpha",
    "userIds": [7, 9, 10]
  }
  ```

***Chat***
- Purpose: Onboarding direct messages and group discussions.
- Key classes: `MessageController`, `MessageService`, `MessageRepository`, DTOs.
- Endpoints:

| Method | Path | Description | Security |
|--------|------|-------------|----------|
| POST | `/api/chat/send` | Send onboarding or group message | Authenticated |
| GET | `/api/chat/onboarding` | Student/admin chat history | Authenticated |
| GET | `/api/chat/group-chat` | Student’s group chat | Authenticated |
| GET | `/api/chat/onboarding/{userId}` | Admin reviews onboarding chat | Admin |
| GET | `/api/chat/group/{groupId}` | Admin sees group chat | Admin |

- Send message request (student → admin):
  ```json
  {
    "content": "I have a question about onboarding."
  }
  ```
- Group message request (admin or group member):
  ```json
  {
    "groupId": 3,
    "content": "Reminder: standup at 10am."
  }
  ```

***Submissions***
- Purpose: Manage assignments, file uploads, evaluations.
- Key classes: `AssignmentController`, `SubmissionController`, `FileController`, services (`AssignmentService`, `SubmissionService`), repositories, DTOs.
- Endpoints:

Assignments:

| Method | Path | Description | Security |
|--------|------|-------------|----------|
| POST | `/api/assignments` | Create assignment for group | Admin |
| GET | `/api/assignments/all` | List all assignments | Admin |
| GET | `/api/assignments` | Student assignments (group filtered) | Authenticated |
| GET | `/api/assignments/{id}` | Assignment detail | Authenticated |
| PUT | `/api/assignments/{id}` | Update assignment | Admin |
| DELETE | `/api/assignments/{id}` | Delete assignment | Admin |
| GET | `/api/assignments/{id}/submissions` | All submissions for assignment | Admin |

Submissions & Files:

| Method | Path | Description | Security |
|--------|------|-------------|----------|
| POST | `/api/submissions` | Upload submission (`multipart/form-data`) | Authenticated |
| GET | `/api/submissions/my` | Own submissions | Authenticated |
| GET | `/api/submissions/{id}` | Submission detail | Authenticated |
| PUT | `/api/submissions/{id}/evaluation` | Grade/feedback | Admin |
| GET | `/api/submissions/assignment/{assignmentId}` | Submissions per assignment | Admin |
| GET | `/api/files/submissions/{submissionId}/download` | Download submission (owner/admin) | Authenticated |
| POST | `/api/files/profile/upload/{type}` | Upload resume/ID/EAD | Authenticated |
| GET | `/api/files/profile/download/{type}` | Download own docs | Authenticated |
| GET | `/api/files/profile/{userId}/download/{type}` | Admin download docs | Admin |
| DELETE | `/api/files/profile/{type}` | Delete own doc | Authenticated |

- Submit assignment form-data example:
  ```
  assignmentId=12
  file=<binary file>
  ```
- Evaluation request:
  ```json
  {
    "status": "REVIEWED",
    "grade": 92.5,
    "feedback": "Great work, add more test coverage."
  }
  ```
- Submission response:
  ```json
  {
    "id": 45,
    "assignmentId": 12,
    "username": "jdoe",
    "fileName": "solution.pdf",
    "status": "REVIEWED",
    "grade": 92.5,
    "submittedAt": "2025-10-08T14:32:10"
  }
  ```

**Notifications & Emails**
- `NotificationEmailService` centralizes templated emails via `EmailTemplate` using `EmailType`.
- Triggered events:
  - Registration success.
  - Direct/group messages (notify recipients).
  - Submission uploads and reviews.
  - Status or group changes.
- `EmailService` uses `JavaMailSender`; configure SMTP credentials and `spring.mail.*` properties.

**Database Schema**
- Entities & relationships:
  - `User` (1) ⟷ (1) `UserDetails` (profile).
  - `User` (many) ⟶ (1) `Group`.
  - `Assignment` (many) ⟶ (1) `Group`.
  - `Submission` (many) ⟶ (1) `Assignment`.
  - `Submission` (many) ⟶ (1) `User`.
  - `Message` (many) ⟶ (1) `User` (sender) and optional `User` (receiver) or `Group`.
- Enum usage: `Role`, `UserStatus`, `SubmissionStatus`, `EmailType`.
- Mermaid ER summary:
  ```mermaid
  erDiagram
    USER ||--|| USER_DETAILS : "has profile"
    USER }o--|| GROUP : "belongs to"
    GROUP ||--o{ ASSIGNMENT : "targets"
    ASSIGNMENT ||--o{ SUBMISSION : "collects"
    USER ||--o{ SUBMISSION : "submits"
    USER ||--o{ MESSAGE : "sends"
    GROUP ||--o{ MESSAGE : "group messages"
  ```
- Persistence:
  - JPA annotations across entities.
  - `@CreationTimestamp`/`@UpdateTimestamp` manage audit columns.
  - Unique submission constraint ensures one submission per assignment+user.

**Coding Conventions & Tips**
- Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`) reduces boilerplate.
- DTOs: records for responses, Lombok classes for requests with validation.
- Exception handling: services throw `RuntimeException`; consider custom exceptions + `@ControllerAdvice` for enhanced error UX.
- File uploads: `FileStorageService` ensures validated extensions and per-user directories. Clean up old files on replacement.
- Method security: prefer `hasRole('ADMIN')` rather than manual role checks in controllers.
- Logging: `logging.level.org.springframework.security=DEBUG` enabled—turn down for prod.
- Future enhancements: central error handling, Flyway migrations, refresh tokens, test coverage expansion.

**Next Steps**
- Run `./mvnw spring-boot:run`.
- Hit `/api/auth/register` then `/api/auth/login` to seed user.
- Create an admin via DB or manual script, assign roles, and start exploring secured endpoints.
