package com.preeti.sansarcart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CartId implements Serializable {

    @Column(name = "customer_user_id")
    private UUID customerId;

    @Column(name = "product_variation_id")
    private UUID productVariationId;
}

