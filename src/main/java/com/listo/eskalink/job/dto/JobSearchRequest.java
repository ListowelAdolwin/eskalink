package com.listo.eskalink.job.dto;

import com.listo.eskalink.common.dto.PageRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobSearchRequest extends PageRequestDto {
    private String title;
    private String location;
    private String companyName;
}
