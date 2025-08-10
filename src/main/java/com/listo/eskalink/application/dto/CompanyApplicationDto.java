package com.listo.eskalink.application.dto;

import com.listo.eskalink.application.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CompanyApplicationDto {
    private UUID id;
    private String applicantName;
    private String resumeLink;
    private String coverLetter;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}
