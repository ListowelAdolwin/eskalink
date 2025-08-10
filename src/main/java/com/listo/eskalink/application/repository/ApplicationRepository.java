package com.listo.eskalink.application.repository;

import com.listo.eskalink.application.entity.Application;
import com.listo.eskalink.application.enums.ApplicationStatus;
import com.listo.eskalink.job.entity.Job;
import com.listo.eskalink.job.enums.JobStatus;
import com.listo.eskalink.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Page<Application> findByApplicant(User applicant, Pageable pageable);

    Page<Application> findByJobAndJobCreatedBy(Job job, User jobCreatedBy, Pageable pageable);

    Page<Application> findByJobAndJobCreatedByAndStatus(Job job, User jobCreatedBy,
                                                        ApplicationStatus status, Pageable pageable);

    boolean existsByApplicantAndJob(User applicant, Job job);

    Optional<Application> findByIdAndJobCreatedBy(UUID id, User jobCreatedBy);

    @Query("SELECT a FROM Application a WHERE a.applicant = :applicant AND " +
            "(:companyName IS NULL OR LOWER(a.job.createdBy.name) LIKE LOWER(CONCAT('%', :companyName, '%'))) AND " +
            "(:jobStatus IS NULL OR a.job.status = :jobStatus) AND " +
            "(:statuses IS NULL OR a.status IN :statuses)")
    Page<Application> findApplicationsWithFilters(@Param("applicant") User applicant,
                                                  @Param("companyName") String companyName,
                                                  @Param("jobStatus") JobStatus jobStatus,
                                                  @Param("statuses") List<ApplicationStatus> statuses,
                                                  Pageable pageable);
}
