package com.preeti.sansarcart.payload.admin;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.preeti.sansarcart.entity.CategoryMetaDataField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

import static com.preeti.sansarcart.common.Util.sanitize;

@Getter
@Setter
public class CategoryMetaFieldDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotBlank(message = "{validation.metadata.field.name.required}")
    @Pattern(regexp = "^(?=.*[a-zA-Z]|$)[a-zA-Z0-9\\s\\-_,.()]*$", message = "{validation.string.invalid}")
    private String name;

    public void setName(String name) {
        this.name = (name != null) ? sanitize(name) : null;
    }

    public static CategoryMetaFieldDto from(CategoryMetaDataField entity) {
        CategoryMetaFieldDto dto = new CategoryMetaFieldDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }


}
