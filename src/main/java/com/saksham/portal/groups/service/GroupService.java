package com.saksham.portal.groups.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saksham.portal.common.enums.Role;
import com.saksham.portal.common.service.NotificationEmailService;
import com.saksham.portal.groups.dto.CreateGroupRequest;
import com.saksham.portal.groups.dto.GroupResponse;
import com.saksham.portal.groups.model.Group;
import com.saksham.portal.groups.repository.GroupRepository;
import com.saksham.portal.users.dto.UserResponse;
import com.saksham.portal.users.model.User;
import com.saksham.portal.users.repository.UserRepository;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final NotificationEmailService notificationEmailService;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository, NotificationEmailService notificationEmailService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.notificationEmailService = notificationEmailService;
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        // Check if group with same name already exists
        if (groupRepository.existsByName(request.name())) {
            throw new RuntimeException("Group with name '" + request.name() + "' already exists");
        }
        
        Group group = new Group();
        group.setName(request.name());
        group.setDescription(request.description());
        Group savedGroup = groupRepository.save(group);

        // Get admin who created the group
        User admin = userRepository.findByRole(Role.ADMIN)
                .stream()
                .findFirst()
                .orElse(null);
        String createdBy = admin != null ? admin.getUsername() : "System Administrator";

        if (request.userIds() != null && !request.userIds().isEmpty()) {
            List<User> users = userRepository.findAllById(request.userIds());
            for (User user : users) {
                savedGroup.addUser(user);
                
                // Send group assignment notification to each user
                notificationEmailService.sendGroupAssignmentNotification(
                    user.getEmail(),
                    group.getName(),
                    createdBy
                );
            }
            userRepository.saveAll(users);
            savedGroup = groupRepository.save(savedGroup);
        }

        // Send group creation notification to all admins
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User adminUser : admins) {
            notificationEmailService.sendGroupCreatedNotification(
                adminUser.getEmail(),
                group.getName(),
                createdBy
            );
        }

        return GroupResponse.fromEntity(savedGroup);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream().map(GroupResponse::fromEntity).toList();
    }

    @Transactional
    public GroupResponse assignUserToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove user from previous group if exists
        if (user.getGroup() != null) {
            user.getGroup().removeUser(user);
        }

        // Add user to new group
        group.addUser(user);
        
        userRepository.save(user);
        Group savedGroup = groupRepository.save(group);

        // Get admin who assigned the user
        User admin = userRepository.findByRole(Role.ADMIN)
                .stream()
                .findFirst()
                .orElse(null);
        String assignedBy = admin != null ? admin.getUsername() : "System Administrator";

        // Send group assignment notification to the user
        notificationEmailService.sendGroupAssignmentNotification(
            user.getEmail(),
            group.getName(),
            assignedBy
        );

        return GroupResponse.fromEntity(savedGroup);
    }

    @Transactional
    public GroupResponse removeUserFromGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is actually in this group
        if (user.getGroup() == null || !user.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("User is not in this group");
        }

        group.removeUser(user);
        
        userRepository.save(user);
        Group savedGroup = groupRepository.save(group);

        return GroupResponse.fromEntity(savedGroup);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersInGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));

        if (group.getUsers() == null) {
            return List.of();
        }

        return group.getUsers().stream()
            .map(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
            ))
            .toList();
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // Remove all users from the group before deleting
        if (group.getUsers() != null && !group.getUsers().isEmpty()) {
            List<User> users = List.copyOf(group.getUsers());
            for (User user : users) {
                group.removeUser(user);
            }
            userRepository.saveAll(users);
        }
        
        groupRepository.delete(group);
    }

    @Transactional
    public GroupResponse updateGroup(Long groupId, CreateGroupRequest request) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // Check if group name is being changed to an existing name
        if (!group.getName().equals(request.name()) && groupRepository.existsByName(request.name())) {
            throw new RuntimeException("Group with name '" + request.name() + "' already exists");
        }
        
        // Update basic group properties
        group.setName(request.name());
        group.setDescription(request.description());
        
        // Get admin who updated the group (assume current admin user for notification purposes)
        User admin = userRepository.findByRole(Role.ADMIN)
                .stream()
                .findFirst()
                .orElse(null);
        String updatedBy = admin != null ? admin.getUsername() : "System Administrator";
        
        // Handle user assignments if provided
        if (request.userIds() != null) {
            // Remove all current users from the group
            if (group.getUsers() != null && !group.getUsers().isEmpty()) {
                List<User> currentUsers = List.copyOf(group.getUsers());
                for (User user : currentUsers) {
                    group.removeUser(user);
                }
                userRepository.saveAll(currentUsers);
            }
            
            // Add new users to the group
            if (!request.userIds().isEmpty()) {
                List<User> newUsers = userRepository.findAllById(request.userIds());
                for (User user : newUsers) {
                    group.addUser(user);
                    
                    // Send group assignment notification to each newly assigned user
                    notificationEmailService.sendGroupAssignmentNotification(
                        user.getEmail(),
                        group.getName(),
                        updatedBy
                    );
                }
                userRepository.saveAll(newUsers);
            }
        }
        
        Group savedGroup = groupRepository.save(group);
        return GroupResponse.fromEntity(savedGroup);
    }
}