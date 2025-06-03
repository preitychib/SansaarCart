package com.preeti.sansarcart.repository.user;

import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.projection.SellerListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Optional<Seller> findFirstByCompanyNameIgnoreCaseOrGstNumberIgnoreCaseOrCompanyContactNumber(
            String companyName, String gstNumber, String contactNumber);
    Page<SellerListView> findAllProjectedBy(Pageable pageable);
    Page<SellerListView> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}