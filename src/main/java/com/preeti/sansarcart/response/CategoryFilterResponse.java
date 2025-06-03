package com.preeti.sansarcart.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CategoryFilterResponse (
        Map<String, Set<String>> metadataMap,
        Set<String> brands,
        BigDecimal minPrice,
        BigDecimal maxPrice
){}
