package com.listo.eskalink.application.dto;

import com.listo.eskalink.application.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApplicationDto {
    private UUID id;
    private UUID applicantId;
    private String applicantName;
    private UUID jobId;
    private String jobTitle;
    private String companyName;
    private String resumeLink;
    private String coverLetter;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}
