package com.listo.eskalink.application.dto;

import com.listo.eskalink.application.enums.ApplicationStatus;
import com.listo.eskalink.common.dto.PageRequestDto;
import com.listo.eskalink.job.enums.JobStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationSearchRequest extends PageRequestDto {
    private String companyName;
    private JobStatus jobStatus;
    private List<ApplicationStatus> applicationStatuses;
}
