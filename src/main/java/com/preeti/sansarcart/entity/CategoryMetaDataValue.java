package com.preeti.sansarcart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category_meta_data_values")
public class CategoryMetaDataValue {

    @EmbeddedId
    private CategoryMetaDataValueId id;

    private String value;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private  Category category;


    @ManyToOne
    @MapsId("categoryMetaDataFieldId")
    @JoinColumn(name ="category_meta_data_field_id", nullable = false)
    private  CategoryMetaDataField categoryMetaDataField;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public CategoryMetaDataValue(Category category,CategoryMetaDataField categoryMetaDataField, String value){
        this.category=category;
        this.categoryMetaDataField=categoryMetaDataField;
        this.value=value;
        this.id=new CategoryMetaDataValueId(category.getId(),categoryMetaDataField.getId());
    }

}


