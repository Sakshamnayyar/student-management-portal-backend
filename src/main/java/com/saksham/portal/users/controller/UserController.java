package com.saksham.portal.users.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saksham.portal.users.dto.BasicUserInfoResponse;
import com.saksham.portal.users.dto.UpdateUserRequest;
import com.saksham.portal.users.dto.UserResponse;
import com.saksham.portal.users.service.UserService;



@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Request Failed: "+e.getMessage());
        }
    }

    @GetMapping("/{id}/basic")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<?> getBasicUserInfo(@PathVariable Long id) {
        try {
            BasicUserInfoResponse user = userService.getBasicUserInfo(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Request Failed: "+e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateuser(
            @PathVariable Long id, 
            @RequestBody UpdateUserRequest request) {
        
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
    

    

    

    
}
