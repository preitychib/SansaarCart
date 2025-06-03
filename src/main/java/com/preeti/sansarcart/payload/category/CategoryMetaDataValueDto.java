package com.preeti.sansarcart.payload.category;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.common.Util;
import com.preeti.sansarcart.entity.Category;
import com.preeti.sansarcart.entity.CategoryMetaDataField;
import com.preeti.sansarcart.entity.CategoryMetaDataValue;
import com.preeti.sansarcart.exception.custom.ValidationException;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.preeti.sansarcart.common.I18n.i18n;
import static com.preeti.sansarcart.common.Util.sanitize;
import static com.preeti.sansarcart.common.Util.validateRequestStrings;

@Getter
@Setter
public class CategoryMetaDataValueDto {

    @NotBlank(message = "{validation.metadata.field.id.required}")
    @JsonProperty("category_meta_data_field_id")
    private UUID categoryMetaDataFieldId;

    @JsonProperty(value = "category_id", access = JsonProperty.Access.READ_ONLY)
    private UUID categoryId;

    @NotBlank(message = "{validation.metadata.value.name.required}")
    private Set<String> values;

    public void setValues(Set<String> values) {
        this.values = values.stream()
                .map(value -> {
                    validateRequestStrings(value);
                    return sanitize(value);
                })
                .collect(Collectors.toSet());
    }
    public void validate() {
        if (values == null || values.isEmpty() ||
                values.stream().allMatch(value -> value == null || value.trim().isEmpty())) {
            throw new ValidationException(i18n("validation.category.meta.data.value.validate"));
        }
    }


    public CategoryMetaDataValue toEntities(Category category, CategoryMetaDataField categoryMetaDataField) {
        String values = String.join(",", this.values);
        return new CategoryMetaDataValue(category, categoryMetaDataField, values);
    }


    // this will receive a group of category metadata value of the same field and collect its values into a set
    private static CategoryMetaDataValueDto convertToDto(List<CategoryMetaDataValue> group) {
        CategoryMetaDataValue first = group.getFirst();
        CategoryMetaDataValueDto dto = new CategoryMetaDataValueDto();
        dto.setCategoryId(first.getId().getCategoryId());
        dto.setCategoryMetaDataFieldId(first.getId().getCategoryMetaDataFieldId());
        dto.setValues(group.stream()
                .map(CategoryMetaDataValue::getValue)
                .flatMap(value -> Set.of(value.split(",")).stream())
                .map(Util::sanitize)
                .collect(Collectors.toSet()));
        return dto;
    }

    // receive list of entities and return list of dto
    // group field so that values can be returned in set
    public static List<CategoryMetaDataValueDto> toDto(List<CategoryMetaDataValue> metaDataValues) {
        return metaDataValues.stream()
                .collect(Collectors.groupingBy(v -> v.getCategoryMetaDataField().getId()))
                .values().stream()
                .map(CategoryMetaDataValueDto::convertToDto)
                .toList();
    }

    // update the values and make sure old one are not deleted
    public void updateEntity(CategoryMetaDataValue entity) {
        Set<String> existingValues = Set.of(entity.getValue().split(","))
                .stream()
                .map(Util::sanitize)
                .collect(Collectors.toSet());
        existingValues.addAll(this.values); // add new values here
        String merged = String.join(",", existingValues);
        entity.setValue(merged);
    }

}
