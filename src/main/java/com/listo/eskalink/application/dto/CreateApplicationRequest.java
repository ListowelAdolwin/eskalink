package com.listo.eskalink.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class CreateApplicationRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    @NotNull(message = "Resume file is required")
    private MultipartFile resume;

    @Size(max = 200, message = "Cover letter must not exceed 200 characters")
    private String coverLetter;
}
