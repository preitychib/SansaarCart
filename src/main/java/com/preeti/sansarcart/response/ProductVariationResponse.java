package com.preeti.sansarcart.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ProductVariationResponse(
        UUID id,
        Long quantityAvailable,
        BigDecimal price,
        JsonNode metadata,
        UUID productId,
        String productName,
        String productDescription,
        String primaryImage,
        List<String> secondaryImage
) {}


