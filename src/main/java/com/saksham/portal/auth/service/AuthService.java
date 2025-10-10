package com.saksham.portal.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.saksham.portal.auth.dto.AuthResponse;
import com.saksham.portal.auth.dto.LoginRequest;
import com.saksham.portal.auth.dto.RegisterRequest;
import com.saksham.portal.auth.util.JwtUtil;
import com.saksham.portal.common.enums.Role;
import com.saksham.portal.common.enums.UserStatus;
import com.saksham.portal.common.service.NotificationEmailService;
import com.saksham.portal.users.model.User;
import com.saksham.portal.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationEmailService notificationEmailService;

    public AuthResponse register(RegisterRequest req) {
        if(userRepo.findByUsername(req.getUsername()).isPresent()){
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .status(UserStatus.ONBOARDING)
                .build();
        
        userRepo.save(user);
        
        // Send registration notification email
        notificationEmailService.sendRegistrationNotification(user.getEmail(), user.getUsername());

        String token = jwtUtil.generateToken(user.getId(), user.getRole().name());
        return new AuthResponse(token, user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User Not found"));
        if(!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name());
        return new AuthResponse(token, user);
    }

    public void changeUserStatus(Long userId, UserStatus newStatus, String reason) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserStatus oldStatus = user.getStatus();
        user.setStatus(newStatus);
        userRepo.save(user);
        
        // Send status change notification if status actually changed
        if (oldStatus != newStatus) {
            notificationEmailService.sendStatusChangeNotification(
                user.getEmail(), 
                newStatus.name(), 
                reason != null ? reason : "Status updated by administrator"
            );
        }
    }
}
