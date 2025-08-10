package com.listo.eskalink.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private Boolean success;
    private String message;
    private T object;
    private List<String> errors;

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data, null);
    }

    public static <T> BaseResponse<T> error(String message, List<String> errors) {
        return new BaseResponse<>(false, message, null, errors);
    }

    public static <T> BaseResponse<T> error(String message, String error) {
        return new BaseResponse<>(false, message, null, error != null ? List.of(error) : null);
    }
}