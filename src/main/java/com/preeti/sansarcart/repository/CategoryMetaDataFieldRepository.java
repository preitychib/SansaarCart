package com.preeti.sansarcart.repository;

import com.preeti.sansarcart.entity.CategoryMetaDataField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryMetaDataFieldRepository extends JpaRepository<CategoryMetaDataField, UUID> {
    boolean existsByNameIgnoreCase(String name);
    Page<CategoryMetaDataField> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
