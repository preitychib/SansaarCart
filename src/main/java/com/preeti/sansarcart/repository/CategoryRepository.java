package com.preeti.sansarcart.repository;

import com.preeti.sansarcart.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByParent(Category parent);
    List<Category> findAllByParentIsNull();
    boolean existsByCategoryNameIgnoreCaseAndParentIsNull(String categoryName);
    Page<Category> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);

    @Query("""
        SELECT DISTINCT c FROM Category c
        LEFT JOIN FETCH c.metaValues mv
        LEFT JOIN FETCH mv.categoryMetaDataField
        WHERE NOT EXISTS (
            SELECT 1 FROM Category child WHERE child.parent = c
        )
    """)
    List<Category> findLeafCategoriesWithMeta();


    @Query(value = """
    WITH RECURSIVE
    upward_tree AS (
        SELECT * FROM categories WHERE id = :categoryId
        UNION ALL
        SELECT c.* FROM categories c
        JOIN upward_tree ut ON ut.parent_id = c.id
    ),
    downward_tree AS (
        SELECT * FROM categories WHERE id = :categoryId
        UNION ALL
        SELECT c.* FROM categories c
        JOIN downward_tree dt ON c.parent_id = dt.id
    )
    SELECT * FROM upward_tree
    UNION
    SELECT * FROM downward_tree
    """, nativeQuery = true)
    List<Category> findFullCategoryHierarchy(@Param("categoryId") UUID categoryId);
}
