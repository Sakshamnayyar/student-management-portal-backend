package com.saksham.portal.users.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saksham.portal.common.eums.Role;
import com.saksham.portal.users.dto.UpdateUserRequest;
import com.saksham.portal.users.dto.UserResponse;
import com.saksham.portal.users.model.User;
import com.saksham.portal.users.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
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

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepo.findById(id).orElseThrow(()-> new RuntimeException("User Not Found"));

        // Debug: Log what we're updating
        System.out.println("Original user: " + user);
        System.out.println("Update request: " + request);

        if (request.username() != null && !request.username().trim().isEmpty()) {
            user.setUsername(request.username());
        }
        if (request.email() != null && !request.email().trim().isEmpty()) {
            user.setEmail(request.email());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }

        // Debug: Log what we're saving
        System.out.println("User before save: " + user);
        
        User savedUser = userRepo.save(user);
        
        // Debug: Log what was saved
        System.out.println("User after save: " + savedUser);

        return toDto(savedUser);
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

    
}
