package com.preeti.sansarcart.repository;

import com.preeti.sansarcart.entity.Category;
import com.preeti.sansarcart.entity.Product;
import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.projection.ProductFilterView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByCategory(Category category);
    Optional<Product> findByProductNameAndBrandAndCategoryAndSeller(
            String productName, String brand, Category category, Seller seller
    );
    Page<Product> findBySeller(Seller seller, Pageable pageable);
    Optional<Product> findByIdAndSeller(UUID id ,Seller seller);

    @Query("""
    SELECT p FROM Product p
    WHERE p.category = :category
      AND p.id <> :id
      AND (:productName IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')))
    """)
    List<Product> findByCategoryAndIdNotFilterByName(@Param("category") Category category,
                                                     @Param("id") UUID id,
                                                     @Param("productName") String productName);


    @Query("""
    SELECT p FROM Product p
    WHERE p.id = :productId
    AND EXISTS (
        SELECT 1 FROM ProductVariation pv
        WHERE pv.product = p
        AND pv.active = true
    )
    """)
    Optional<Product> findByIdAndHasActiveVariations(@Param("productId") UUID productId);


    @Query("SELECT p FROM Product p WHERE "
            + "(:categoryId IS NULL OR p.category.id = :categoryId) "
            + "AND (:sellerId IS NULL OR p.seller.id = :sellerId) "
            + "AND (:name IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :name, '%'))) "
            + "AND (:isActive IS NULL OR p.active = :isActive)")
    Page<Product> findAllWithFilters(@Param("categoryId") UUID categoryId,
                                     @Param("sellerId") UUID sellerId,
                                     @Param("name") String name,
                                     @Param("isActive") Boolean isActive,
                                     Pageable pageable);


    @Query("""
        SELECT p.brand AS brandName, v.price AS price
        FROM Product p
        LEFT JOIN p.variations v ON v.active = true
        WHERE p.category IN :categories
        AND p.active = true
    """)
    List<ProductFilterView> findBrandAndPriceProjectionByCategories(@Param("categories") List<Category> categories);


}
