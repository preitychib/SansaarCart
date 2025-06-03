package com.preeti.sansarcart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
public class Cart extends AuditInfo{

    @EmbeddedId
    private CartId id;

    @MapsId("customerId")
    @ManyToOne
    @JoinColumn(name = "customer_user_id")
    private Customer customer;

    @MapsId("productVariationId")
    @ManyToOne
    @JoinColumn(name = "product_variation_id")
    private ProductVariation productVariation;

    private Long quantity;

    private Boolean isWishListItem;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
