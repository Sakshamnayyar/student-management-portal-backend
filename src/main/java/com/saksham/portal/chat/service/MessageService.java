package com.saksham.portal.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.saksham.portal.chat.dto.MessageRequest;
import com.saksham.portal.chat.dto.MessageResponse;
import com.saksham.portal.chat.model.Message;
import com.saksham.portal.chat.repository.MessageRepository;
import com.saksham.portal.common.enums.Role;
import com.saksham.portal.common.service.NotificationEmailService;
import com.saksham.portal.groups.model.Group;
import com.saksham.portal.groups.repository.GroupRepository;
import com.saksham.portal.users.model.User;
import com.saksham.portal.users.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class MessageService {

    private MessageRepository messageRepo;
    private GroupRepository groupRepo;
    private UserRepository userRepo;
    private NotificationEmailService notificationEmailService;

    public MessageService(
                    MessageRepository messageRepo,
                    GroupRepository groupRepo,
                    UserRepository userRepo,
                    NotificationEmailService notificationEmailService) {
        this.messageRepo = messageRepo;
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
        this.notificationEmailService = notificationEmailService;
    }

    @Transactional
    public MessageResponse sendMessage(Long senderId, MessageRequest request) {

        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender Not Found"));

        Message msg = new Message();
        msg.setSender(sender);
        msg.setContent(request.content());
        msg.setTimestamp(LocalDateTime.now());

        // Handle receiver ID based on sender role
        if(request.groupId() == null) { // This is onboarding chat
            if(sender.getRole() == Role.USER) {
                // For regular users, automatically set receiver to admin
                User admin = userRepo.findByRole(Role.ADMIN)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Admin not found"));
                msg.setReceiver(admin);
                
                // Send email notification to admin about new direct message
                notificationEmailService.sendDirectMessageNotification(
                    admin.getEmail(),
                    sender.getUsername(),
                    request.content()
                );
            } else if(sender.getRole() == Role.ADMIN) {
                // For admin, receiver ID must be provided
                if(request.receiverId() == null) {
                    throw new RuntimeException("Admin must specify receiver ID when sending messages");
                }
                User receiver = userRepo.findById(request.receiverId())
                            .orElseThrow(()-> new RuntimeException("Receiver not found"));
                msg.setReceiver(receiver);
                
                // Send email notification to user about admin message
                notificationEmailService.sendDirectMessageNotification(
                    receiver.getEmail(),
                    sender.getUsername(),
                    request.content()
                );
            }
        }
        
        if(request.groupId() != null) {
            Group group = groupRepo.findById(request.groupId())
                        .orElseThrow(()-> new RuntimeException("Group not found"));
            msg.setGroup(group);
            // Send email notifications to all group members except the sender
            if (group.getUsers() != null) {
                group.getUsers().stream()
                    .filter(member -> !Objects.equals(member.getId(), senderId)) // Don't notify sender
                    .forEach(member -> {
                        notificationEmailService.sendGroupMessageNotification(
                            member.getEmail(),
                            group.getName(),
                            sender.getUsername(),
                            request.content()
                        );
                    });
            }
        }
        
        Message saved = messageRepo.save(msg);

        return new MessageResponse(
            saved.getId(),
            saved.getSender().getId(),
            saved.getReceiver()!=null ? saved.getReceiver().getId() : null,
            saved.getGroup()!=null ? saved.getGroup().getId() : null,
            saved.getContent(),
            saved.getTimestamp()
        );
    }

    public List<MessageResponse> getOnboardingChat(Long userId) {
    User admin = userRepo.findByRole(Role.ADMIN)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Admin not found"));

    return messageRepo.findOnboardingChat(userId, admin.getId())
            .stream()
            .map(this::toDto)
            .toList();
    }

    public List<MessageResponse> getGroupChat(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        
        if (user.getGroup() == null) {
            return List.of(); // Return empty list if user has no group
        }
        
        return messageRepo.findGroupChat(user.getGroup().getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Admin APIs
    public List<MessageResponse> getOnboardingChatByUserId(Long userId) {
        // Verify user exists
        userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        
        User admin = userRepo.findByRole(Role.ADMIN)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return messageRepo.findOnboardingChat(userId, admin.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<MessageResponse> getGroupChatByGroupId(Long groupId) {
        // Verify group exists
        groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        return messageRepo.findGroupChat(groupId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private MessageResponse toDto(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getSender()!=null ? message.getSender().getId() : null,
            message.getReceiver()!=null ? message.getReceiver().getId() : null,
            message.getGroup()!=null ? message.getGroup().getId(): null,
            message.getContent(),
            message.getTimestamp()
        );
    }
}
