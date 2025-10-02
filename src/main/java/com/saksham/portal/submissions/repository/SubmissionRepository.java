package com.saksham.portal.submissions.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saksham.portal.submissions.model.Submission;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);

    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);

    boolean existsByAssignmentIdAndUserId(Long assignmentId, Long userId);
}
