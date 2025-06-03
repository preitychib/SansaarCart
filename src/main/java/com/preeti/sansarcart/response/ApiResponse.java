package com.preeti.sansarcart.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private MetaData metadata;
    private T details;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data, null,null);
    }

    public static <T> ApiResponse<T> success(String message, T data, MetaData metadata) {
        return new ApiResponse<>("success", message, data, metadata,null);
    }

    public static <T> ApiResponse<T> error(String message,T details) {
        return new ApiResponse<>("error", message, null, null,details);
    }
}
