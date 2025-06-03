package com.preeti.sansarcart.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMetaDataValueId implements Serializable {

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "category_meta_data_field_id", nullable = false)
    private UUID categoryMetaDataFieldId;
}
