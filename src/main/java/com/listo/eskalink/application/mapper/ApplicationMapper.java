package com.listo.eskalink.application.mapper;

import com.listo.eskalink.application.dto.ApplicationDto;
import com.listo.eskalink.application.dto.ApplicantApplicationDto;
import com.listo.eskalink.application.dto.CompanyApplicationDto;
import com.listo.eskalink.application.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(source = "applicant.id", target = "applicantId")
    @Mapping(source = "applicant.name", target = "applicantName")
    @Mapping(source = "job.id", target = "jobId")
    @Mapping(source = "job.title", target = "jobTitle")
    @Mapping(source = "job.createdBy.name", target = "companyName")
    ApplicationDto applicationToApplicationDto(Application application);

    @Mapping(source = "job.title", target = "jobTitle")
    @Mapping(source = "job.createdBy.name", target = "companyName")
    @Mapping(source = "job.status", target = "jobStatus")
    ApplicantApplicationDto applicationToApplicantApplicationDto(Application application);

    @Mapping(source = "applicant.name", target = "applicantName")
    CompanyApplicationDto applicationToCompanyApplicationDto(Application application);
}
