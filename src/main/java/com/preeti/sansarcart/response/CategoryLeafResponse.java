package com.preeti.sansarcart.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CategoryLeafResponse(
        UUID id,
        String categoryName,
        List<String> parentChain,
        Set<MetaDataFieldValue> metaData
) {

    public record MetaDataFieldValue(
            String fieldName,
            List<String> values
    ) {}
}

