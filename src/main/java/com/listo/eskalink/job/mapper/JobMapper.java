package com.listo.eskalink.job.mapper;

import com.listo.eskalink.job.dto.*;
import com.listo.eskalink.job.entity.Job;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Job createJobRequestToJob(CreateJobRequest request);

    @Mapping(source = "createdBy.name", target = "companyName")
    @Mapping(source = "createdBy.id", target = "companyId")
    @Mapping(target = "applicationCount", ignore = true)
    JobDto jobToJobDto(Job job);

    @Mapping(source = "createdBy.name", target = "companyName")
    JobListDto jobToJobListDto(Job job);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateJobFromRequest(UpdateJobRequest request, @MappingTarget Job job);
}
