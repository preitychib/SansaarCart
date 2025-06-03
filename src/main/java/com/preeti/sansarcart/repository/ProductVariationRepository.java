package com.preeti.sansarcart.repository;

import com.preeti.sansarcart.entity.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariationRepository extends JpaRepository<ProductVariation, UUID> {
    List<ProductVariation> findByProductId(UUID productId);

    @Query("""
    SELECT pv FROM ProductVariation pv
    JOIN FETCH pv.product p
    WHERE pv.id = :id
    """)
    Optional<ProductVariation> findByIdWithProduct(UUID id);

}
