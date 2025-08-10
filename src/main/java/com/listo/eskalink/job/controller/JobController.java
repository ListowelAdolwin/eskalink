package com.listo.eskalink.job.controller;


import com.listo.eskalink.common.dto.BaseResponse;
import com.listo.eskalink.common.dto.PaginatedResponse;
import com.listo.eskalink.job.dto.*;
import com.listo.eskalink.job.service.JobService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Jobs", description = "Job management endpoints")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Create Job", description = "Create a new job posting (Company only)")
    public ResponseEntity<BaseResponse<JobDto>> createJob(
            @Valid @RequestBody CreateJobRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Create job request from company: {}", userDetails.getUserId());

        JobDto jobDto = jobService.createJob(request, userDetails.getUserId());
        BaseResponse<JobDto> response = BaseResponse.success("Job created successfully", jobDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Update Job", description = "Update an existing job posting (Company only)")
    public ResponseEntity<BaseResponse<JobDto>> updateJob(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID jobId,
            @Valid @RequestBody UpdateJobRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Update job request for job: {} from company: {}", jobId, userDetails.getUserId());

        JobDto jobDto = jobService.updateJob(jobId, request, userDetails.getUserId());
        BaseResponse<JobDto> response = BaseResponse.success("Job updated successfully", jobDto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Delete Job", description = "Delete a job posting (Company only)")
    public ResponseEntity<BaseResponse<String>> deleteJob(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID jobId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Delete job request for job: {} from company: {}", jobId, userDetails.getUserId());

        jobService.deleteJob(jobId, userDetails.getUserId());
        BaseResponse<String> response = BaseResponse.success("Job deleted successfully", null);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Browse Jobs", description = "Search and browse available job postings (Applicant only)")
    public ResponseEntity<PaginatedResponse<JobListDto>> searchJobs(
            @Parameter(description = "Job title filter") @RequestParam(required = false) String title,
            @Parameter(description = "Location filter") @RequestParam(required = false) String location,
            @Parameter(description = "Company name filter") @RequestParam(required = false) String companyName,
            @Parameter(description = "Page number (default: 1)") @RequestParam(defaultValue = "1") Integer pageNumber,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "Sort by field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {

        JobSearchRequest request = new JobSearchRequest();
        request.setTitle(title);
        request.setLocation(location);
        request.setCompanyName(companyName);
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        List<JobListDto> jobs = jobService.searchJobs(request);

        PaginatedResponse<JobListDto> response = PaginatedResponse.success(
                "Jobs retrieved successfully",
                jobs,
                pageNumber,
                pageSize,
                (long) jobs.size()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get Job Details", description = "Get detailed information about a specific job")
    public ResponseEntity<BaseResponse<JobDto>> getJobDetails(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID jobId) {

        log.info("Get job details request for job: {}", jobId);

        JobDto jobDto = jobService.getJobDetails(jobId);
        BaseResponse<JobDto> response = BaseResponse.success("Job details retrieved successfully", jobDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Get Company Jobs", description = "Get list of jobs posted by the company")
    public ResponseEntity<PaginatedResponse<JobDto>> getCompanyJobs(
            @Parameter(description = "Job status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (default: 1)") @RequestParam(defaultValue = "1") Integer pageNumber,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "Sort by field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        CompanyJobsRequest request = new CompanyJobsRequest();
        if (status != null) {
            try {
                request.setStatus(com.listo.eskalink.job.enums.JobStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        PaginatedResponse.error("Invalid job status", List.of("Invalid job status: " + status))
                );
            }
        }
        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        List<JobDto> jobs = jobService.getCompanyJobs(request, userDetails.getUserId());

        PaginatedResponse<JobDto> response = PaginatedResponse.success(
                "Company jobs retrieved successfully",
                jobs,
                pageNumber,
                pageSize,
                (long) jobs.size()
        );

        return ResponseEntity.ok(response);
    }
}
