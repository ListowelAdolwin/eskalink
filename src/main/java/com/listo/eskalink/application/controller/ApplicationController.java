package com.listo.eskalink.application.controller;


import com.listo.eskalink.application.dto.*;
import com.listo.eskalink.application.enums.ApplicationStatus;
import com.listo.eskalink.application.service.ApplicationService;
import com.listo.eskalink.common.dto.BaseResponse;
import com.listo.eskalink.common.dto.PaginatedResponse;
import com.listo.eskalink.job.enums.JobStatus;
import com.listo.eskalink.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Applications", description = "Job application management endpoints")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Apply for Job", description = "Submit job application with resume (Applicant only)")
    public ResponseEntity<BaseResponse<ApplicationDto>> applyForJob(
            @Parameter(description = "Job ID", required = true) @RequestParam UUID jobId,
            @Parameter(description = "Resume file (PDF/DOCX)", required = true) @RequestParam MultipartFile resume,
            @Parameter(description = "Cover letter (optional)") @RequestParam(required = false) String coverLetter,
            Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Job application request from applicant: {} for job: {}", userDetails.getUserId(), jobId);

        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setJobId(jobId);
        request.setResume(resume);
        request.setCoverLetter(coverLetter);

        ApplicationDto applicationDto = applicationService.applyForJob(request, userDetails.getUserId());
        BaseResponse<ApplicationDto> response = BaseResponse.success("Application submitted successfully", applicationDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Track My Applications", description = "Get list of jobs applied to by the applicant")
    public ResponseEntity<PaginatedResponse<ApplicantApplicationDto>> getMyApplications(
            @Parameter(description = "Company name filter") @RequestParam(required = false) String companyName,
            @Parameter(description = "Job status filter") @RequestParam(required = false) String jobStatus,
            @Parameter(description = "Application statuses filter (comma-separated)") @RequestParam(required = false) String applicationStatuses,
            @Parameter(description = "Page number (default: 1)") @RequestParam(defaultValue = "1") Integer pageNumber,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "Sort by field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        ApplicationSearchRequest request = new ApplicationSearchRequest();
        request.setCompanyName(companyName);

        if (jobStatus != null) {
            try {
                request.setJobStatus(JobStatus.valueOf(jobStatus.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        PaginatedResponse.error("Invalid job status", List.of("Invalid job status: " + jobStatus))
                );
            }
        }

        if (applicationStatuses != null) {
            try {
                List<ApplicationStatus> statuses = List.of(applicationStatuses.split(","))
                        .stream()
                        .map(status -> ApplicationStatus.valueOf(status.trim().toUpperCase()))
                        .collect(Collectors.toList());
                request.setApplicationStatuses(statuses);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        PaginatedResponse.error("Invalid application status", List.of("Invalid application status"))
                );
            }
        }

        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        List<ApplicantApplicationDto> applications = applicationService.getApplicantApplications(request, userDetails.getUserId());

        PaginatedResponse<ApplicantApplicationDto> response = PaginatedResponse.success(
                "Applications retrieved successfully",
                applications,
                pageNumber,
                pageSize,
                (long) applications.size()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "View Job Applications", description = "Get applications for a specific job (Company only)")
    public ResponseEntity<PaginatedResponse<CompanyApplicationDto>> getJobApplications(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID jobId,
            @Parameter(description = "Application status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (default: 1)") @RequestParam(defaultValue = "1") Integer pageNumber,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") Integer pageSize,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Get job applications request for job: {} from company: {}", jobId, userDetails.getUserId());

        ApplicationStatus applicationStatus = null;
        if (status != null) {
            try {
                applicationStatus = ApplicationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        PaginatedResponse.error("Invalid application status", List.of("Invalid application status: " + status))
                );
            }
        }

        List<CompanyApplicationDto> applications = applicationService.getJobApplications(
                jobId, applicationStatus, pageNumber, pageSize, userDetails.getUserId()
        );

        PaginatedResponse<CompanyApplicationDto> response = PaginatedResponse.success(
                "Job applications retrieved successfully",
                applications,
                pageNumber,
                pageSize,
                (long) applications.size()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Update Application Status", description = "Update status of a job application (Company only)")
    public ResponseEntity<BaseResponse<ApplicationDto>> updateApplicationStatus(
            @Parameter(description = "Application ID", required = true) @PathVariable UUID applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Update application status request for application: {} from company: {}", applicationId, userDetails.getUserId());

        ApplicationDto applicationDto = applicationService.updateApplicationStatus(
                applicationId, request, userDetails.getUserId()
        );

        BaseResponse<ApplicationDto> response = BaseResponse.success("Application status updated successfully", applicationDto);

        return ResponseEntity.ok(response);
    }
}
