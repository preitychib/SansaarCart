package com.preeti.sansarcart.repository;

import com.preeti.sansarcart.entity.Role;
import com.preeti.sansarcart.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByAuthority(RoleType authority);
}