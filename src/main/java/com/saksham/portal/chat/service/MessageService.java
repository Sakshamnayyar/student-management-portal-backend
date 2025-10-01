package com.saksham.portal.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.saksham.portal.chat.dto.MessageRequest;
import com.saksham.portal.chat.dto.MessageResponse;
import com.saksham.portal.chat.model.Message;
import com.saksham.portal.chat.repository.MessageRepository;
import com.saksham.portal.groups.model.Group;
import com.saksham.portal.groups.repository.GroupRepository;
import com.saksham.portal.users.model.User;
import com.saksham.portal.users.repository.UserRepository;
import com.saksham.portal.common.enums.Role;

import jakarta.transaction.Transactional;

@Service
public class MessageService {

    private MessageRepository messageRepo;
    private GroupRepository groupRepo;
    private UserRepository userRepo;

    public MessageService(
                    MessageRepository messageRepo,
                    GroupRepository groupRepo,
                    UserRepository userRepo) {
        this.messageRepo = messageRepo;
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
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
        if(request.groupId() == null) { // This is onboarding chat (one-on-one)
            if(sender.getRole() == Role.USER) {
                // For regular users, automatically set receiver to admin
                User admin = userRepo.findByRole(Role.ADMIN)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Admin not found"));
                msg.setReceiver(admin);
            } else if(sender.getRole() == Role.ADMIN) {
                // For admin, receiver ID must be provided
                if(request.receiverId() == null) {
                    throw new RuntimeException("Admin must specify receiver ID when sending messages");
                }
                User receiver = userRepo.findById(request.receiverId())
                            .orElseThrow(()-> new RuntimeException("Receiver not found"));
                msg.setReceiver(receiver);
            }
        }
        
        if(request.groupId() != null) {
            Group group = groupRepo.findById(request.groupId())
                        .orElseThrow(()-> new RuntimeException("Group not found"));
            msg.setGroup(group);
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
        User user = userRepo.findById(userId)
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
