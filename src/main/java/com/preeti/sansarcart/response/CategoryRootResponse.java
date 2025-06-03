package com.preeti.sansarcart.response;

import java.util.UUID;
public record CategoryRootResponse(
        UUID id,
        String categoryName
) {}
