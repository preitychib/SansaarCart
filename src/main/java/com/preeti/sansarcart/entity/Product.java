package com.preeti.sansarcart.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE products SET is_deleted = true WHERE id = ?")
@SQLRestriction(value = "is_deleted = false")
public class Product extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "seller_user_id")
    private Seller seller;

    @Column(nullable = false)
    private String productName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String brand;

    @Column(name = "is_cancellable")
    private Boolean cancellable=false;

    @Column(name = "is_returnable")
    private Boolean returnable=false;

    @Column(name = "is_active")
    private Boolean active=false;

    @Column(name = "is_deleted")
    private Boolean deleted=false;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE}, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductVariation> variations = new ArrayList<>();


}
