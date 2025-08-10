package com.listo.eskalink.job.dto;

import com.listo.eskalink.job.enums.JobStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JobListDto {
    private UUID id;
    private String title;
    private String description;
    private String location;
    private JobStatus status;
    private String companyName;
    private LocalDateTime createdAt;
}
