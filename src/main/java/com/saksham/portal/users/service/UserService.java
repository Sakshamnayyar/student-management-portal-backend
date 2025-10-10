package com.saksham.portal.users.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saksham.portal.common.enums.Role;
import com.saksham.portal.common.enums.UserStatus;
import com.saksham.portal.common.service.NotificationEmailService;
import com.saksham.portal.users.dto.BasicUserInfoResponse;
import com.saksham.portal.users.dto.UpdateUserRequest;
import com.saksham.portal.users.dto.UserResponse;
import com.saksham.portal.users.model.User;
import com.saksham.portal.users.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final NotificationEmailService notificationEmailService;

    public UserService(UserRepository userRepo, NotificationEmailService notificationEmailService) {
        this.userRepo = userRepo;
        this.notificationEmailService = notificationEmailService;
    }

    public List<UserResponse> getAllUsers() {
        return userRepo.findByRole(Role.USER)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return userRepo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public BasicUserInfoResponse getBasicUserInfo(Long id) {
        return userRepo.findById(id)
                .map(this::toBasicDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepo.findById(id).orElseThrow(()-> new RuntimeException("User Not Found"));

        // Track changes for notification purposes
        boolean statusChanged = false;
        UserStatus oldStatus = user.getStatus();
        UserStatus newStatus = request.status();

        if (request.username() != null) user.setUsername(request.username());
        if (request.email() != null) user.setEmail(request.email());
        if (request.role() != null) user.setRole(request.role());
        if (request.status() != null) {
            user.setStatus(request.status());
            statusChanged = !oldStatus.equals(newStatus);
        }

        UserResponse result = toDto(userRepo.save(user));

        // Send email notification if status changed
        if (statusChanged) {
            String reason = buildStatusChangeReason(oldStatus, newStatus);
            notificationEmailService.sendStatusChangeNotification(
                user.getEmail(),
                newStatus.name(),
                reason
            );
        }

        return result;
    }

    @Transactional
    public void updateUserStatus(Long userId, UserStatus newStatus, String reason) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserStatus oldStatus = user.getStatus();
        if (!oldStatus.equals(newStatus)) {
            user.setStatus(newStatus);
            userRepo.save(user);
            
            // Send status change notification
            notificationEmailService.sendStatusChangeNotification(
                user.getEmail(),
                newStatus.name(),
                reason != null ? reason : buildStatusChangeReason(oldStatus, newStatus)
            );
        }
    }

    private String buildStatusChangeReason(UserStatus oldStatus, UserStatus newStatus) {
        return String.format("Status updated from %s to %s by administrator", 
            oldStatus.name(), newStatus.name());
    }

    private UserResponse toDto(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getStatus(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    private BasicUserInfoResponse toBasicDto(User user) {
        return new BasicUserInfoResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
    }
}
