package com.preeti.sansarcart.service;

import com.preeti.sansarcart.common.Util;
import com.preeti.sansarcart.entity.Category;
import com.preeti.sansarcart.entity.CategoryMetaDataField;
import com.preeti.sansarcart.entity.CategoryMetaDataValue;
import com.preeti.sansarcart.exception.custom.ListValidationException;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.admin.CategoryMetaFieldDto;
import com.preeti.sansarcart.payload.category.CategoryDto;
import com.preeti.sansarcart.payload.category.CategoryMetaDataValueDto;
import com.preeti.sansarcart.projection.ProductFilterView;
import com.preeti.sansarcart.repository.CategoryMetaDataFieldRepository;
import com.preeti.sansarcart.repository.CategoryMetaDataValueRepository;
import com.preeti.sansarcart.repository.CategoryRepository;
import com.preeti.sansarcart.repository.ProductRepository;

import com.preeti.sansarcart.response.CategoryFilterResponse;
import com.preeti.sansarcart.response.CategoryLeafResponse;
import com.preeti.sansarcart.response.CategoryRootResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.preeti.sansarcart.common.I18n.i18n;
import static com.preeti.sansarcart.common.Util.sanitize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryMetaDataFieldRepository categoryMetaDataFieldRepository;
    private final CategoryMetaDataValueRepository categoryMetaDataValueRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryMetaFieldDto createCategoryMetaDataField(String name) {
        if (categoryMetaDataFieldRepository.existsByNameIgnoreCase(name))
            throw new ValidationException(i18n("validation.metadata.field.name.unique"));
        CategoryMetaDataField metaDataField = new CategoryMetaDataField(name);
        categoryMetaDataFieldRepository.save(metaDataField);
        return CategoryMetaFieldDto.from(metaDataField);
    }

    public List<CategoryMetaFieldDto> getAllMetaDataFields(int page, int size, String sortBy, String sortDir, String name) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CategoryMetaDataField> resultPage;
        if (StringUtils.hasText(name)) {
            resultPage = categoryMetaDataFieldRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            resultPage = categoryMetaDataFieldRepository.findAll(pageable);
        }

        return resultPage.getContent()
                .stream()
                .map(CategoryMetaFieldDto::from)
                .toList();
    }


    public CategoryDto createCategory(String categoryName, UUID parentId) {
        Category category = new Category(categoryName);
        if (parentId == null) {
            if (categoryRepository.existsByCategoryNameIgnoreCaseAndParentIsNull(categoryName)) {
                throw new ValidationException(i18n("validation.category.name.unique"));
            }
            return CategoryDto.from(categoryRepository.save(category));
        }

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.category.not.found")));

        if (productRepository.existsByCategory(parent)) {
            throw new ValidationException(i18n("validation.category.parent.dependency.exists"));
        }

        if (!checkCategoryUniqueName(categoryName, parent)) {
            throw new ValidationException(i18n("validation.category.name.unique"));
        }

        category.setParent(parent);
        return CategoryDto.from(categoryRepository.save(category));
    }

    private boolean checkCategoryUniqueName(String categoryName, Category parent) {
        Category root = getRootAndCheckNameValid(categoryName, parent);
        if (root.getCategoryName().equalsIgnoreCase(categoryName)) {
            throw new ValidationException(i18n("validation.category.name.unique"));
        }

        return checkImmediateChildHasUniqueName(parent,categoryName);
    }


    public CategoryFilterResponse getCategoryFilteringDetails(UUID categoryId) {
        Category givenCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.category.not.found")));

        List<Category> categoriesInHierarchy = categoryRepository.findFullCategoryHierarchy(categoryId);

        Map<String, Set<String>> metadataMap = new LinkedHashMap<>();
        for (Category category : categoriesInHierarchy) {
            for (CategoryMetaDataValue meta : category.getMetaValues()) {
                String fieldName = sanitize(meta.getCategoryMetaDataField().getName());
                Set<String> values = Arrays.stream(meta.getValue().split(","))
                        .map(Util::sanitize)
                        .collect(Collectors.toSet());

                metadataMap.merge(fieldName, values, (existing, incoming) -> {
                    existing.addAll(incoming);
                    return existing;
                });
            }
        }

        Set<UUID> parentIds = categoriesInHierarchy.stream()
                .map(Category::getParent)
                .filter(Objects::nonNull)
                .map(Category::getId)
                .collect(Collectors.toSet());

        List<Category> leafCategories = categoriesInHierarchy.stream()
                .filter(cat -> !parentIds.contains(cat.getId()))
                .toList();

        List<ProductFilterView> productData = productRepository.findBrandAndPriceProjectionByCategories(leafCategories);
        Set<String> brands = new HashSet<>();
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        for (ProductFilterView view : productData) {
            brands.add(view.getBrandName());

            BigDecimal price = view.getPrice();
            if (price != null) {
                if (minPrice == null || price.compareTo(minPrice) < 0) {
                    minPrice = price;
                }
                if (maxPrice == null || price.compareTo(maxPrice) > 0) {
                    maxPrice = price;
                }
            }
        }
        return new CategoryFilterResponse(metadataMap, brands, minPrice, maxPrice);
    }


    private Category getRootAndCheckNameValid(String categoryName, Category current) {
        while (current.getParent() != null) {
            if (current.getCategoryName().equalsIgnoreCase(categoryName)) {
                throw new ValidationException(i18n("validation.category.name.unique"));
            }
            current = current.getParent();
        }
        return current;
    }

    private boolean checkImmediateChildHasUniqueName(Category parent, String categoryName){
        Set<String> names = new HashSet<>();
        // adding to check if any other does not exist
        names.add(categoryName.toLowerCase());
        List<Category> children = categoryRepository.findAllByParent(parent);
        for (Category child : children) {
            if (!names.add(child.getCategoryName().toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkAllChildHasUniqueCategoryName(Category category, String categoryName) {
        Set<String> names = new HashSet<>();
        Queue<Category> queue = new LinkedList<>();
        queue.add(category);
        // adding to check if any other does not exist
        names.add(categoryName.toLowerCase());
        while (!queue.isEmpty()) {
            Category current = queue.poll();
            List<Category> children = categoryRepository.findAllByParent(current);
            for (Category child : children) {
                if (!names.add(child.getCategoryName().toLowerCase())) {
                    return false;
                }
                queue.add(child);
            }
        }
        return true;
    }

    public List<CategoryDto> getAllCategories(int page, int size, String sortBy, String sortDir, String categoryName) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Category> resultPage;
        if (StringUtils.hasText(categoryName)) {
            resultPage = categoryRepository.findByCategoryNameContainingIgnoreCase(categoryName, pageable);
        } else {
            resultPage = categoryRepository.findAll(pageable);
        }

        return resultPage.getContent()
                .stream()
                .map(CategoryDto::from)
                .toList();
    }


    public List<CategoryDto> viewCategoryHierarchy(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.category.not.found")));
        List<CategoryDto> categoryHierarchy = new ArrayList<>();
        categoryHierarchy.add(CategoryDto.from(category));
        Category current = category.getParent();
        while (current != null) {
            categoryHierarchy.addFirst(CategoryDto.from(current));
            current = current.getParent();
        }
        List<Category> children = categoryRepository.findAllByParent(category);
        List<CategoryDto> childDtoList = children.stream()
                .map(CategoryDto::from).toList();
        categoryHierarchy.addAll(childDtoList);
        return categoryHierarchy;
    }

    public CategoryDto updateCategory(String updateName, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.category.not.found")));
        if (category.getParent() == null) {
            if (categoryRepository.existsByCategoryNameIgnoreCaseAndParentIsNull(updateName)) {
                throw new ValidationException(i18n("validation.category.name.unique"));
            }
        } else {
            Category parent = category.getParent();
            // this check if the parent does not have any child with the given name
            // and the child of the current node does not have any child with given name
            if (!checkCategoryUniqueName(updateName, parent) && !checkAllChildHasUniqueCategoryName(category, updateName)) {
                throw new ValidationException(i18n("validation.category.name.unique"));
            }
        }
        category.setCategoryName(updateName);
        return CategoryDto.from(categoryRepository.save(category));
    }

    @Transactional
    public List<CategoryMetaDataValueDto> saveCategoryMetaDataValue(UUID categoryId, List<CategoryMetaDataValueDto> categoryMetaDataValueDtoList,boolean isUpdate) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFound(i18n("exception.category.not.found")));
        List<CategoryMetaDataValue> allEntities = validateCategoryMetaDataValueDtoList(category, categoryMetaDataValueDtoList,isUpdate);
        List<CategoryMetaDataValue> savedEntities = categoryMetaDataValueRepository.saveAll(allEntities);
        return CategoryMetaDataValueDto.toDto(savedEntities);
    }

    private List<CategoryMetaDataValue> validateCategoryMetaDataValueDtoList(Category category, List<CategoryMetaDataValueDto> dtoList,boolean isUpdate) {
        List<CategoryMetaDataValue> validEntities = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (CategoryMetaDataValueDto dto : dtoList) {
            try {
                dto.validate();
                CategoryMetaDataField field = categoryMetaDataFieldRepository.findById(dto.getCategoryMetaDataFieldId())
                        .orElse(null);
                if (field == null) {
                    errors.add("Field [" + dto.getCategoryMetaDataFieldId() + "]: " + i18n("exception.category.metadata.field.not.found"));
                    continue;
                }

                CategoryMetaDataValue existingValue = categoryMetaDataValueRepository
                        .findByCategoryAndCategoryMetaDataField(category, field)
                        .orElse(null);

                if (existingValue == null) {
                    if (isUpdate) {
                        errors.add("Field [" + dto.getCategoryMetaDataFieldId() + "]: " + i18n("validation.category.meta.data.value.not.found"));
                        continue;
                    }
                    validEntities.add(dto.toEntities(category, field));
                } else {
                    if (isUpdate) {
                        // update only if the entity exists
                        dto.updateEntity(existingValue);
                        validEntities.add(existingValue);
                    } else {
                        errors.add("Field [" + dto.getCategoryMetaDataFieldId() + "]: " + i18n("validation.category.meta.data.value.duplicate"));
                        continue;
                    }
                }

            } catch (RuntimeException e) {
                errors.add("Field [" + dto.getCategoryMetaDataFieldId() + "]: " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new ListValidationException(errors);
        }

        return validEntities;
    }


    @Transactional(readOnly = true)
    public List<CategoryRootResponse> getRootCategories(UUID parentId) {
        List<Category> categories;

        if (parentId == null) {
            categories = categoryRepository.findAllByParentIsNull();
        } else {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFound(i18n("exception.category.not.found")));
            categories = categoryRepository.findAllByParent(parent);
        }

        return categories.stream()
                .map(cat -> new CategoryRootResponse(cat.getId(), cat.getCategoryName()))
                .collect(Collectors.toList());
    }




    @Transactional(readOnly = true)
    public List<CategoryLeafResponse> getAllLeafCategories() {
        List<Category> leafCategories = categoryRepository.findLeafCategoriesWithMeta();

        return leafCategories.stream()
                .map(this::mapToResponse)
                .collect(toList());
    }

    private CategoryLeafResponse mapToResponse(Category category) {
        Set<CategoryLeafResponse.MetaDataFieldValue> metaData = category.getMetaValues().stream()
                .map(val -> new CategoryLeafResponse.MetaDataFieldValue(
                        val.getCategoryMetaDataField().getName(),
                        Arrays.stream(val.getValue().split(",")).toList()
                ))
                .collect(toSet());

        List<String> parentChain = buildParentChain(category);

        return new CategoryLeafResponse(
                category.getId(),
                category.getCategoryName(),
                parentChain,
                metaData
        );
    }

    private List<String> buildParentChain(Category category) {
        List<String> chain = new ArrayList<>();
        Category current = category.getParent();
        while (current != null) {
            chain.add(current.getCategoryName());
            current = current.getParent();
        }
        Collections.reverse(chain);
        return chain;
    }

}

