package com.saksham.portal.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.saksham.portal.users.model.UserDetails;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    

    Optional<UserDetails> findByUserId(Long userId);
    

    Optional<UserDetails> findByUser_Username(String username);
    
    boolean existsByUserId(Long userId);

    @Query("SELECT COUNT(ud) FROM UserDetails ud WHERE ud.profileCompleted = true")
    long countCompletedProfiles();
}
