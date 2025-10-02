package com.saksham.portal.users.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_details")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "referral_source", length = 500)
    private String referralSource;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "ead_type", length = 100)
    private String eadType;

    @Column(name = "ead_start_date")
    private LocalDate eadStartDate;

    @Column(name = "prior_experience")
    @Builder.Default
    private Boolean priorExperience = false;

    @Column(name = "experience_details", columnDefinition = "TEXT")
    private String experienceDetails;

    @Column(name = "programming_languages", length = 1000)
    private String programmingLanguages;

    @Column(name = "ead_file_path", length = 500)
    private String eadFilePath;

    @Column(name = "id_file_path", length = 500)
    private String idFilePath;

    @Column(name = "resume_file_path", length = 500)
    private String resumeFilePath;

    @Column(name = "profile_completed")
    @Builder.Default
    private Boolean profileCompleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}