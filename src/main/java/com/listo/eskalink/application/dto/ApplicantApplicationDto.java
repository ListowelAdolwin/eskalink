package com.listo.eskalink.application.dto;

import com.listo.eskalink.application.enums.ApplicationStatus;
import com.listo.eskalink.job.enums.JobStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApplicantApplicationDto {
    private UUID id;
    private String jobTitle;
    private String companyName;
    private ApplicationStatus status;
    private JobStatus jobStatus;
    private LocalDateTime appliedAt;
}
