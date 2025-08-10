package com.listo.eskalink.job.dto;

import com.listo.eskalink.job.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateJobRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 1, max = 100, message = "Job title must be between 1 and 100 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    @Size(min = 20, max = 2000, message = "Job description must be between 20 and 2000 characters")
    private String description;

    private String location;

    private JobStatus status = JobStatus.DRAFT;
}
