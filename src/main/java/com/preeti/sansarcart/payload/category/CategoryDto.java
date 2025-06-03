package com.preeti.sansarcart.payload.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

import static com.preeti.sansarcart.common.Util.sanitize;

@Getter
@Setter
public class CategoryDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotBlank(message = "{validation.category.name.required}")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    @JsonProperty("category_name")
     private String categoryName;

    @JsonProperty("parent_id")
    private UUID parentId;


    public void setCategoryName(String categoryName) {
        this.categoryName = sanitize(categoryName);
    }

    public static CategoryDto from(Category entity) {
        CategoryDto dto = new CategoryDto();
        BeanUtils.copyProperties(entity, dto);
        dto.setParentId(entity.getParent() != null ? entity.getParent().getId() : null);
        return dto;
    }

}
