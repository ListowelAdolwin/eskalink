package com.listo.eskalink.job.repository;

import com.listo.eskalink.job.entity.Job;
import com.listo.eskalink.job.enums.JobStatus;
import com.listo.eskalink.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    Page<Job> findByCreatedBy(User createdBy, Pageable pageable);

    Page<Job> findByCreatedByAndStatus(User createdBy, JobStatus status, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE " +
            "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:companyName IS NULL OR LOWER(j.createdBy.name) LIKE LOWER(CONCAT('%', :companyName, '%')))")
    Page<Job> findJobsWithFilters(@Param("title") String title,
                                  @Param("location") String location,
                                  @Param("companyName") String companyName,
                                  Pageable pageable);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId")
    Long countApplicationsByJobId(@Param("jobId") UUID jobId);

    Optional<Job> findByIdAndCreatedBy(UUID id, User createdBy);
}
