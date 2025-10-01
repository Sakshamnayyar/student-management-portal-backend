package com.saksham.portal.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saksham.portal.auth.dto.AuthResponse;
import com.saksham.portal.auth.dto.LoginRequest;
import com.saksham.portal.auth.dto.RegisterRequest;
import com.saksham.portal.auth.service.AuthService;
import com.saksham.portal.common.service.EmailService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            emailService.sendEmail(request.getEmail(), 
                    "Successful Registeration", 
                    "This is email sent from Student Management Portal to notify you about successfull registeration");
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
    
    
    
}
