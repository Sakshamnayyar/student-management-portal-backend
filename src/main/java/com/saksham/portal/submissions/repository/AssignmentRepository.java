package com.saksham.portal.submissions.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saksham.portal.submissions.model.Assignment;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCreatedById(Long createdById);

    List<Assignment> findByGroupIdOrderByDueDateAsc(Long groupId);

    List<Assignment> findAllByOrderByCreatedAtDesc();
}
