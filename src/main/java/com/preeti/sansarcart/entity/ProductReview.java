package com.preeti.sansarcart.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


// can be embedded?
@Entity
@Table(name = "product_reviews", uniqueConstraints = {
            @UniqueConstraint(columnNames =
                    {"customer_user_id", "product_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview extends AuditInfo{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(columnDefinition = "TEXT")
    private String review;

    @Min(value = 1, message = "{common.rating.min}")
    @Max(value = 5, message = "{common.rating.max}")
    private Integer rating;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name="customer_user_id")
    private Customer customer;

}
