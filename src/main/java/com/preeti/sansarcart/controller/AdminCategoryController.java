package com.preeti.sansarcart.controller;

import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.admin.CategoryMetaFieldDto;
import com.preeti.sansarcart.payload.category.CategoryDto;
import com.preeti.sansarcart.payload.category.CategoryMetaDataValueDto;
import com.preeti.sansarcart.response.ApiResponse;
import com.preeti.sansarcart.response.MetaData;
import com.preeti.sansarcart.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.preeti.sansarcart.common.I18n.i18n;
import static com.preeti.sansarcart.common.Util.sanitize;
import static com.preeti.sansarcart.common.Util.validateRequestStrings;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("admin")
@Secured(value ="ROLE_ADMIN")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping("/category-meta-data-field")
    public ResponseEntity<ApiResponse<CategoryMetaFieldDto>> createCategoryMetaDataField(@Valid @RequestBody CategoryMetaFieldDto metaDataField) {
        log.info("Admin: Creating category metadata field with name: {}", metaDataField.getName());

        CategoryMetaFieldDto metaFieldDto = categoryService.createCategoryMetaDataField(metaDataField.getName());

        log.info("Admin: Category metadata field created: {}", metaFieldDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("admin.category.metadata.field.created"), metaFieldDto));
    }

    @GetMapping("/category-meta-data-field/all")
    public ResponseEntity<ApiResponse<List<CategoryMetaFieldDto>>> getAllMetaDataFields(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name
    ) {
        log.info("Admin: Fetching category metadata fields with filters - page: {}, size: {}, sortBy: {}, sortDir: {}, name: {}",
                page, size, sortBy, sortDir, name);


        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new ValidationException(i18n("validation.sort.direction.invalid", sortDir));
        }

        Map<String, String> filters = new HashMap<>();
        if (StringUtils.hasText(name)) {
            sanitize(name);
            validateRequestStrings(name);
            filters.put("name", name);
        }

        List<CategoryMetaFieldDto> metaFieldDtoList = categoryService.getAllMetaDataFields(page, size, sortBy, sortDir, name);
        MetaData metaData = MetaData.ofPagination(size, page, sortBy, sortDir, filters);
        log.info("Admin: Fetched {} category metadata fields", metaFieldDtoList.size());
        return ResponseEntity.ok(ApiResponse.success(i18n("admin.category.metadata.fields.fetched"), metaFieldDtoList, metaData));
    }

    @PostMapping("/category")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@Valid @RequestBody CategoryDto dto) {
        log.info("Admin: Creating category with name: {} and parentId: {}", dto.getCategoryName(), dto.getParentId());
        CategoryDto created = categoryService.createCategory(dto.getCategoryName(), dto.getParentId());
        log.info("Admin: Category created: {}", created);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("admin.category.created"), created));
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String categoryName
    ) {
        log.info("Admin: Fetching categories with filters - page: {}, size: {}, sortBy: {}, sortDir: {}, categoryName: {}",
                page, size, sortBy, sortDir, categoryName);

        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new ValidationException(i18n("validation.sort.direction.invalid", sortDir));
        }

        Map<String, String> filters = new HashMap<>();
        if (StringUtils.hasText(categoryName)) {
            filters.put("category_name", categoryName);
        }

        List<CategoryDto> categoryDtoList = categoryService.getAllCategories(page, size, sortBy, sortDir, categoryName);
        MetaData metaData = MetaData.ofPagination(size, page, sortBy, sortDir, filters);

        log.info("Admin: Fetched {} categories", categoryDtoList.size());
        return ResponseEntity.ok(ApiResponse.success(i18n("admin.category.all.fetched"), categoryDtoList, metaData));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategory(@PathVariable UUID id) {
        log.info("Admin: Fetching category hierarchy for categoryId: {}", id);

        List<CategoryDto> categoryDtoList = categoryService.viewCategoryHierarchy(id);

        log.info("Admin: Fetched category hierarchy for categoryId: {}", id);
        return ResponseEntity.ok(ApiResponse.success(i18n("admin.category.fetched"), categoryDtoList));
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Admin: Updating category with id: {} and new name: {}", id, categoryDto.getCategoryName());

        CategoryDto updatedDto = categoryService.updateCategory(categoryDto.getCategoryName(), id);

        log.info("Admin: Category with id: {} updated successfully", id);
        return ResponseEntity.ok(ApiResponse.success(i18n("admin.category.updated"), updatedDto));
    }

    @PostMapping("/category/{categoryId}/metadata-values")
    public ResponseEntity<ApiResponse<List<CategoryMetaDataValueDto>>> saveCategoryMetaDataValue(
            @PathVariable UUID categoryId,
            @RequestBody List<CategoryMetaDataValueDto> categoryMetaDataValueDtoList) {
        log.info("Admin: Saving metadata values for categoryId: {}", categoryId);

        List<CategoryMetaDataValueDto> savedValues = categoryService.saveCategoryMetaDataValue(categoryId, categoryMetaDataValueDtoList, false);

        log.info("Admin: Saved {} metadata values for categoryId: {}", savedValues.size(), categoryId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("admin.category.meta.data.value.created"), savedValues));
    }

    @PutMapping("category/{categoryId}/metadata-values")
    public ResponseEntity<ApiResponse<List<CategoryMetaDataValueDto>>> updateCategoryMetaDataValue(
            @PathVariable UUID categoryId,
            @RequestBody List<CategoryMetaDataValueDto> categoryMetaDataValueDtoList) {
        log.info("Admin: Updating metadata values for categoryId: {}", categoryId);
        List<CategoryMetaDataValueDto> updatedValues = categoryService.saveCategoryMetaDataValue(categoryId, categoryMetaDataValueDtoList, true);
        log.info("Admin: Updated {} metadata values for categoryId: {}", updatedValues.size(), categoryId);
        return ResponseEntity.ok(ApiResponse.success(i18n("admin.category.meta.data.value.updated"), updatedValues));
    }
}