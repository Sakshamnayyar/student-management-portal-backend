package com.saksham.portal.groups.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saksham.portal.groups.model.Group;

public interface GroupRepository extends JpaRepository<Group, Long>{
    
    Optional<Group> findByName(String name);
    
    boolean existsByName(String name);
}
