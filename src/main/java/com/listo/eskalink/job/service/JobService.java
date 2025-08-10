package com.listo.eskalink.job.service;

import com.listo.eskalink.common.dto.PageRequestDto;
import com.listo.eskalink.common.exception.ResourceNotFoundException;
import com.listo.eskalink.common.exception.UnauthorizedException;
import com.listo.eskalink.common.exception.ValidationException;
import com.listo.eskalink.job.dto.*;
import com.listo.eskalink.job.entity.Job;
import com.listo.eskalink.job.mapper.JobMapper;
import com.listo.eskalink.job.repository.JobRepository;
import com.listo.eskalink.user.entity.User;
import com.listo.eskalink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @Transactional
    public JobDto createJob(CreateJobRequest request, UUID userId) {
        log.info("Creating job for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobMapper.createJobRequestToJob(request);
        job.setCreatedBy(user);

        job = jobRepository.save(job);
        log.info("Job created successfully with ID: {}", job.getId());

        return jobMapper.jobToJobDto(job);
    }

    @Transactional
    public JobDto updateJob(UUID jobId, UpdateJobRequest request, UUID userId) {
        log.info("Updating job: {} by user: {}", jobId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepository.findByIdAndCreatedBy(jobId, user)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized access"));

        if (request.getStatus() != null && !job.getStatus().canTransitionTo(request.getStatus())) {
            throw new ValidationException("Invalid status transition from " + job.getStatus() + " to " + request.getStatus());
        }

        jobMapper.updateJobFromRequest(request, job);
        job = jobRepository.save(job);

        log.info("Job updated successfully: {}", jobId);
        return jobMapper.jobToJobDto(job);
    }

    @Transactional
    public void deleteJob(UUID jobId, UUID userId) {
        log.info("Deleting job: {} by user: {}", jobId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepository.findByIdAndCreatedBy(jobId, user)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized access"));

        jobRepository.delete(job);
        log.info("Job deleted successfully: {}", jobId);
    }

    public List<JobListDto> searchJobs(JobSearchRequest request) {
        log.info("Searching jobs with filters: title={}, location={}, company={}",
                request.getTitle(), request.getLocation(), request.getCompanyName());

        Pageable pageable = createPageable(request);

        Page<Job> jobs = jobRepository.findJobsWithFilters(
                request.getTitle(),
                request.getLocation(),
                request.getCompanyName(),
                pageable
        );

        return jobs.getContent().stream()
                .map(jobMapper::jobToJobListDto)
                .toList();
    }

    public JobDto getJobDetails(UUID jobId) {
        log.info("Getting job details for: {}", jobId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        return jobMapper.jobToJobDto(job);
    }

    public List<JobDto> getCompanyJobs(CompanyJobsRequest request, UUID userId) {
        log.info("Getting jobs for company: {} with status filter: {}", userId, request.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = createPageable(request);

        Page<Job> jobs;
        if (request.getStatus() != null) {
            jobs = jobRepository.findByCreatedByAndStatus(user, request.getStatus(), pageable);
        } else {
            jobs = jobRepository.findByCreatedBy(user, pageable);
        }

        return jobs.getContent().stream()
                .map(job -> {
                    JobDto jobDto = jobMapper.jobToJobDto(job);
                    Long applicationCount = jobRepository.countApplicationsByJobId(job.getId());
                    jobDto.setApplicationCount(applicationCount);
                    return jobDto;
                })
                .toList();
    }

    private Pageable createPageable(PageRequestDto request) {
        Sort sort = Sort.unsorted();

        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, request.getSortBy());
        }

        return PageRequest.of(request.getPageNumber() - 1, request.getPageSize(), sort);
    }
}
