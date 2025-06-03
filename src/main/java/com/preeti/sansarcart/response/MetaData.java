package com.preeti.sansarcart.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaData {
    private int max;
    private int offset;
    private String sort;
    private String order;
    private Map<String, String> filter;

    public static MetaData ofPagination(int size, int page, String sort, String order, Map<String, String> filter) {
        int offset = page * size;
        return new MetaData(size, offset, sort, order, filter);
    }
}

