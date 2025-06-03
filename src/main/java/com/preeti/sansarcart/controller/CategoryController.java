package com.preeti.sansarcart.controller;

import com.preeti.sansarcart.response.ApiResponse;
import com.preeti.sansarcart.response.CategoryFilterResponse;
import com.preeti.sansarcart.response.CategoryLeafResponse;
import com.preeti.sansarcart.response.CategoryRootResponse;
import com.preeti.sansarcart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.preeti.sansarcart.common.I18n.i18n;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("seller/categories")
    public ResponseEntity<ApiResponse<List<CategoryLeafResponse>>> getAllLeafCategories() {
        log.info("Seller: Fetching all leaf categories");
        List<CategoryLeafResponse> leafCategories = categoryService.getAllLeafCategories();
        log.info("Seller: Leaf categories fetched successfully");
        return ResponseEntity.ok(ApiResponse.success(i18n("category.fetch.success"), leafCategories));
    }

    @GetMapping("customer/categories")
    public ResponseEntity<ApiResponse<List<CategoryRootResponse>>> getAllRootCategories(
            @RequestParam(value = "parentCategoryId", required = false) UUID parentCategoryId) {
        log.info("Customer: Fetching root categories for parentCategoryId: {}", parentCategoryId);
        List<CategoryRootResponse> rootCategories = categoryService.getRootCategories(parentCategoryId);
        log.info("Customer: Root categories fetched successfully for parentCategoryId: {}", parentCategoryId);
        return ResponseEntity.ok(ApiResponse.success(i18n("category.fetch.success"), rootCategories));
    }

    @GetMapping("customer/category/{categoryId}/filter-details")
    public ResponseEntity<ApiResponse<CategoryFilterResponse>> getAllFilterDetails(
            @PathVariable UUID categoryId) {
        log.info("Customer: Fetching filter details for categoryId: {}", categoryId);
        CategoryFilterResponse categoryFilterData = categoryService.getCategoryFilteringDetails(categoryId);
        log.info("Customer: Filter details fetched successfully for categoryId: {}", categoryId);
        return ResponseEntity.ok(ApiResponse.success(i18n("category.fetch.success"), categoryFilterData));
    }
}
