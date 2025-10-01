package com.saksham.portal.users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saksham.portal.common.eums.Role;
import com.saksham.portal.users.model.User;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

}
