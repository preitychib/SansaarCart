package com.preeti.sansarcart.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ProductViewResponse(
        UUID id,
        String name,
        String brand,
        String description,
        Boolean isCancellable,
        Boolean isReturnable,
        CategoryDetail category
        , List<ProductVariationDetail> variations
        , Double similarityScore
        ) {
    public record CategoryDetail(
            UUID id,
            String name
    ) {}
    public record ProductVariationDetail(
            UUID id,
            BigDecimal price,
            String primary_image
    ) {}
}
