package com.preeti.sansarcart.repository.user;


import com.preeti.sansarcart.entity.Customer;
import com.preeti.sansarcart.projection.UserListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,UUID> {
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    Page<UserListView> findAllProjectedBy(Pageable pageable);
    Page<UserListView> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}