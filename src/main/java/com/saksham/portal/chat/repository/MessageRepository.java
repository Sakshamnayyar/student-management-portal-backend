package com.saksham.portal.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.saksham.portal.chat.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long>{

    @Query("SELECT m FROM Message m " +
        "WHERE (m.sender.id = :userId AND m.receiver.id = :adminId) " +
        "   OR (m.receiver.id = :userId AND m.sender.id = :adminId) " +
        "ORDER BY m.timestamp ASC")
    List<Message> findOnboardingChat(@Param("userId") Long userId,
                                    @Param("adminId") Long adminId);

    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId ORDER BY m.timestamp ASC")
    List<Message> findGroupChat(@Param("groupId") Long groupId);

    
}
