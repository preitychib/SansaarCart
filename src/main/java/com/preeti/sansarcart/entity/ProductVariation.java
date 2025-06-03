package com.preeti.sansarcart.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE product_variations SET is_deleted = true WHERE id = ?")
@SQLRestriction(value = "is_deleted = false")
public class ProductVariation extends AuditInfo{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;


    private Long quantityAvailable;

    private BigDecimal price;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private JsonNode metaData;

    private String primaryImageName;

    @Column(name = "is_active",nullable = false)
    private Boolean active=false;

    @Column(name = "is_deleted")
    private Boolean deleted=false;

}
