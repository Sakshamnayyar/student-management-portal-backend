package com.saksham.portal.groups.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saksham.portal.groups.model.Group;

public interface GroupRepository extends JpaRepository<Group, Long>{
}
