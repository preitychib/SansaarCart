package com.preeti.sansarcart.repository;

import com.preeti.sansarcart.entity.Address;
import com.preeti.sansarcart.enums.AddressLabelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    Optional<Address> findFirstByUserIdAndLabel(UUID userId, AddressLabelType label);
    Optional<Address> findByIdAndUserId(UUID addressId, UUID userId);
    List<Address> findAllByUserId(UUID userId);
}