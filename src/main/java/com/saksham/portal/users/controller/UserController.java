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

import com.saksham.portal.users.dto.UpdateUserRequest;
import com.saksham.portal.users.dto.UserResponse;
import com.saksham.portal.users.service.UserService;



@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Request Failed: "+e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateuser(
            @PathVariable Long id, 
            @RequestBody UpdateUserRequest request) {
        
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
    

    

    

    
}
