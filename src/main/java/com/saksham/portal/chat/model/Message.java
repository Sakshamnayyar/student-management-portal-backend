package com.saksham.portal.chat.model;

import java.time.LocalDateTime;

import com.saksham.portal.users.model.User;
import com.saksham.portal.groups.model.Group;

import jakarta.persistence.Entity;
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
@Table(name= "messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;//only used for onboarding chat

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group; //null for onboarding

    private String content;

    private LocalDateTime timestamp;

    @PrePersist
    void onCreate() {
        timestamp = LocalDateTime.now();
    }
}

