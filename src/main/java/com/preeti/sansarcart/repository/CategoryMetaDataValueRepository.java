package com.preeti.sansarcart.repository;

import com.preeti.sansarcart.entity.Category;
import com.preeti.sansarcart.entity.CategoryMetaDataField;
import com.preeti.sansarcart.entity.CategoryMetaDataValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryMetaDataValueRepository extends JpaRepository<CategoryMetaDataValue, UUID> {
    Optional<CategoryMetaDataValue> findByCategoryAndCategoryMetaDataField(Category category, CategoryMetaDataField field);

}
