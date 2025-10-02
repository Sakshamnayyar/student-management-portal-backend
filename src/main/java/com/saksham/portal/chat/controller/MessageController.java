package com.saksham.portal.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saksham.portal.auth.util.JwtUtil;
import com.saksham.portal.chat.dto.MessageRequest;
import com.saksham.portal.chat.dto.MessageResponse;
import com.saksham.portal.chat.service.MessageService;

@RestController
@RequestMapping("api/chat")
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    public MessageController(MessageService messageService, JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody MessageRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long senderId = jwtUtil.extractUserId(token);
            MessageResponse response = messageService.sendMessage(senderId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Message Failed to send: " + e.getMessage());
        }
    }

    @GetMapping("/onboarding")
    public ResponseEntity<?> getOnboardingChat(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            return ResponseEntity.ok(messageService.getOnboardingChat(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get onboarding chat: " + e.getMessage());
        }
    }

    @GetMapping("/group-chat")
    public ResponseEntity<List<MessageResponse>> getGroupChat(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            return ResponseEntity.ok(messageService.getGroupChat(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin endpoints
    @GetMapping("/onboarding/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MessageResponse>> getOnboardingChatByUserId(
            @PathVariable Long userId) {
        try {
            return ResponseEntity.ok(messageService.getOnboardingChatByUserId(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MessageResponse>> getGroupChatByGroupId(
            @PathVariable Long groupId) {
        try {
            return ResponseEntity.ok(messageService.getGroupChatByGroupId(groupId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}