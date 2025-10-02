package com.saksham.portal.submissions.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saksham.portal.groups.model.Group;
import com.saksham.portal.groups.repository.GroupRepository;
import com.saksham.portal.submissions.dto.AssignmentRequest;
import com.saksham.portal.submissions.dto.AssignmentResponse;
import com.saksham.portal.submissions.model.Assignment;
import com.saksham.portal.submissions.repository.AssignmentRepository;
import com.saksham.portal.users.model.User;
import com.saksham.portal.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public AssignmentResponse createAssignment(Long createdById, AssignmentRequest request) {
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .group(group)
                .createdBy(creator)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return toResponse(saved);
    }

    @Transactional
    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDate(request.getDueDate());

        if (!assignment.getGroup().getId().equals(request.getGroupId())) {
            Group newGroup = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found"));
            assignment.setGroup(newGroup);
        }

        Assignment updated = assignmentRepository.save(assignment);
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByGroup(Long groupId) {
        return assignmentRepository.findByGroupIdOrderByDueDateAsc(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByCreator(Long creatorId) {
        return assignmentRepository.findByCreatedById(creatorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByUserGroup(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getGroup() == null) {
            return List.of();
        }
        
        return getAssignmentsByGroup(user.getGroup().getId());
    }

    @Transactional
    public void deleteAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        assignmentRepository.delete(assignment);
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getGroup().getId(),
                assignment.getGroup().getName(),
                assignment.getCreatedBy().getId(),
                assignment.getCreatedBy().getUsername(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt(),
                (long) assignment.getSubmissions().size()
        );
    }
}