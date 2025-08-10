package com.listo.eskalink.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private Boolean success;
    private String message;
    private List<T> object;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalSize;
    private List<String> errors;

    public static <T> PaginatedResponse<T> success(String message, List<T> data,
                                                   int pageNumber, int pageSize, long totalSize) {
        return new PaginatedResponse<>(true, message, data, pageNumber, pageSize, totalSize, null);
    }

    public static <T> PaginatedResponse<T> error(String message, List<String> errors) {
        return new PaginatedResponse<>(false, message, null, null, null, null, errors);
    }
}
