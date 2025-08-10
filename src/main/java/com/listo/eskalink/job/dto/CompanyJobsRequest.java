package com.listo.eskalink.job.dto;

import com.listo.eskalink.common.dto.PageRequestDto;
import com.listo.eskalink.job.enums.JobStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyJobsRequest extends PageRequestDto {
    private JobStatus status;
}
