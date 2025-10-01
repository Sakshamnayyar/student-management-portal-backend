package com.saksham.portal.groups.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saksham.portal.groups.dto.CreateGroupRequest;
import com.saksham.portal.groups.dto.GroupResponse;
import com.saksham.portal.groups.service.GroupService;
import com.saksham.portal.users.dto.UserResponse;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService){
        this.groupService = groupService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupResponse> createGroup(@RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(groupService.createGroup(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
        
    }

    @PutMapping("/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable Long groupId, 
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroup(groupId, request));
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{groupId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignUserToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try{
            GroupResponse response = groupService.assignUserToGroup(groupId, userId);
            return ResponseEntity.ok(response);
        }catch(Exception e) {
            return ResponseEntity.badRequest().body("Failed: "+e.getMessage());
        }
    }

    @DeleteMapping("/{groupId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        try{
            GroupResponse response = groupService.removeUserFromGroup(groupId, userId);
            return ResponseEntity.ok(response);
        }catch(Exception e) {
            return ResponseEntity.badRequest().body("Failed: "+e.getMessage());
        }
    }

    @GetMapping("/{groupId}/users")
    public ResponseEntity<?> getUsersInGroup(@PathVariable Long groupId) {
        try{
            List<UserResponse> response = groupService.getUsersInGroup(groupId);
            return ResponseEntity.ok(response);
        }catch(Exception e) {
            return ResponseEntity.badRequest().body("Failed: "+e.getMessage());
        }
    }
    

}
