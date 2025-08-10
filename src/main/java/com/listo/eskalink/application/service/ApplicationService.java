package com.listo.eskalink.application.service;


import com.listo.eskalink.application.dto.*;
import com.listo.eskalink.application.entity.Application;
import com.listo.eskalink.application.enums.ApplicationStatus;
import com.listo.eskalink.application.mapper.ApplicationMapper;
import com.listo.eskalink.application.repository.ApplicationRepository;
import com.listo.eskalink.common.exception.ResourceNotFoundException;
import com.listo.eskalink.common.exception.UnauthorizedException;
import com.listo.eskalink.common.exception.ValidationException;
import com.listo.eskalink.common.service.FileUploadService;
import com.listo.eskalink.job.entity.Job;
import com.listo.eskalink.job.repository.JobRepository;
import com.listo.eskalink.user.entity.User;
import com.listo.eskalink.user.repository.UserRepository;
import com.listo.eskalink.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final FileUploadService fileUploadService;
    private final EmailService emailService;

    @Transactional
    public ApplicationDto applyForJob(CreateApplicationRequest request, UUID applicantId) throws IOException {
        log.info("Processing job application for job: {} by applicant: {}", request.getJobId(), applicantId);

        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found"));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (applicationRepository.existsByApplicantAndJob(applicant, job)) {
            throw new ValidationException("You have already applied for this job");
        }

        String resumeUrl = fileUploadService.uploadResume(request.getResume());

        Application application = Application.builder()
                .applicant(applicant)
                .job(job)
                .resumeLink(resumeUrl)
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.APPLIED)
                .build();

        application = applicationRepository.save(application);

        emailService.sendJobApplicationNotification(
                job.getCreatedBy().getEmail(),
                job.getCreatedBy().getName(),
                job.getTitle(),
                applicant.getName()
        );

        log.info("Job application created successfully with ID: {}", application.getId());
        return applicationMapper.applicationToApplicationDto(application);
    }

    public List<ApplicantApplicationDto> getApplicantApplications(ApplicationSearchRequest request, UUID applicantId) {
        log.info("Getting applications for applicant: {}", applicantId);

        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant not found"));

        Pageable pageable = createPageable(request);

        Page<Application> applications = applicationRepository.findApplicationsWithFilters(
                applicant,
                request.getCompanyName(),
                request.getJobStatus(),
                request.getApplicationStatuses(),
                pageable
        );

        return applications.getContent().stream()
                .map(applicationMapper::applicationToApplicantApplicationDto)
                .toList();
    }

    public List<CompanyApplicationDto> getJobApplications(UUID jobId, ApplicationStatus status,
                                                          int pageNumber, int pageSize, UUID companyId) {
        log.info("Getting applications for job: {} by company: {}", jobId, companyId);

        User company = userRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCreatedBy().getId().equals(companyId)) {
            throw new UnauthorizedException("Unauthorized access");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("appliedAt").descending());

        Page<Application> applications;
        if (status != null) {
            applications = applicationRepository.findByJobAndJobCreatedByAndStatus(job, company, status, pageable);
        } else {
            applications = applicationRepository.findByJobAndJobCreatedBy(job, company, pageable);
        }

        return applications.getContent().stream()
                .map(applicationMapper::applicationToCompanyApplicationDto)
                .toList();
    }

    @Transactional
    public ApplicationDto updateApplicationStatus(UUID applicationId, UpdateApplicationStatusRequest request, UUID companyId) {
        log.info("Updating application status: {} to: {} by company: {}", applicationId, request.getStatus(), companyId);

        User company = userRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Application application = applicationRepository.findByIdAndJobCreatedBy(applicationId, company)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(request.getStatus());
        application = applicationRepository.save(application);

        if (shouldSendStatusUpdateEmail(request.getStatus())) {
            emailService.sendApplicationStatusUpdate(
                    application.getApplicant().getEmail(),
                    application.getApplicant().getName(),
                    application.getJob().getTitle(),
                    application.getJob().getCreatedBy().getName(),
                    request.getStatus()
            );
        }

        log.info("Application status updated from {} to {} for application: {}",
                oldStatus, request.getStatus(), applicationId);

        return applicationMapper.applicationToApplicationDto(application);
    }

    private boolean shouldSendStatusUpdateEmail(ApplicationStatus status) {
        return status == ApplicationStatus.INTERVIEW ||
                status == ApplicationStatus.REJECTED ||
                status == ApplicationStatus.HIRED;
    }

    private Pageable createPageable(ApplicationSearchRequest request) {
        Sort sort = Sort.by("appliedAt").descending();

        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, request.getSortBy());
        }

        return PageRequest.of(request.getPageNumber() - 1, request.getPageSize(), sort);
    }
}