package com.saksham.portal.submissions.model;

import java.time.LocalDateTime;

import com.saksham.portal.common.eums.SubmissionStatus;
import com.saksham.portal.common.eums.SubmissionType;
import com.saksham.portal.users.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private SubmissionType type;

    private Integer weekNumber;

    @Column(nullable=false)
    private String fileUrl;

    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @PrePersist
    void onCreate() {
        submittedAt = LocalDateTime.now();
        if(status == null) status = SubmissionStatus.PENDING;
    }
    
}
