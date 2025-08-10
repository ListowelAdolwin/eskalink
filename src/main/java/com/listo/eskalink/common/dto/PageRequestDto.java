package com.listo.eskalink.common.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequestDto {
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNumber = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    private Integer pageSize = 10;

    private String sortBy;
    private String sortDirection = "asc";
}
