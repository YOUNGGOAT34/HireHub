package com.goat.HireHub.job.requirement;
import com.goat.HireHub.company.Role;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.Job;
import com.goat.HireHub.job.JobService;
import com.goat.HireHub.job.RequirementType;
import com.goat.HireHub.job.requirement.dto.JobRequirementPatchRequest;
import com.goat.HireHub.job.requirement.dto.JobRequirementRequest;
import com.goat.HireHub.job.requirement.dto.JobRequirementResponse;
import com.goat.HireHub.job.requirement.dto.JobRequirementsResponse;
import com.goat.HireHub.security.SecurityUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobRequirementService {
    private final JobRequirementsRepository jobRequirementsRepository;
    private final SecurityUtils securityUtils;
    private final JobService jobService;


    public JobRequirementService(JobRequirementsRepository jobRequirementsRepository,
                                 SecurityUtils securityUtils,
                                 JobService jobService
                                 ){
         this.jobRequirementsRepository=jobRequirementsRepository;
         this.securityUtils=securityUtils;
         this.jobService=jobService;

    }

    @Transactional(readOnly = true)
    public JobRequirementsResponse getJobRequirements(Long jobId){
        Map<RequirementType,List<JobRequirementResponse>> groupedRequirements=jobRequirementsRepository.findByJobIdOrderByDisplayOrderAsc(jobId)
                 .stream()
                .collect(Collectors.groupingBy(
                        JobRequirement::getRequirementType,
                        Collectors.mapping(JobRequirementResponse::from,Collectors.toList())
                ));

        return new JobRequirementsResponse(
             jobId,
             groupedRequirements.getOrDefault(RequirementType.RESPONSIBILITY,List.of()),
             groupedRequirements.getOrDefault(RequirementType.QUALIFICATION,List.of()),
             groupedRequirements.getOrDefault(RequirementType.PREFERRED_QUALIFICATION,List.of()),
             groupedRequirements.getOrDefault(RequirementType.NICE_TO_HAVE,List.of())
        );

    }

    @Transactional
    public JobRequirementResponse addJobRequirement(Long jobId, JobRequirementRequest request){
          authorizeAccess(jobId);
          Job job=jobService.findOrThrow(jobId);
          JobRequirement requirement=new JobRequirement();
          requirement.setJob(job);
          requirement.setRequirementType(request.requirementType());
          requirement.setContent(request.content());
          Integer position= request.position();
          if (position==null){
              position=jobRequirementsRepository.nextPosition(jobId,request.requirementType().name());
          }else{
              jobRequirementsRepository.shiftPositionDownForPost(jobId,request.requirementType().name(),position);
          }
          requirement.setDisplayOrder(position);
          JobRequirement savedRequirement=jobRequirementsRepository.save(requirement);
          return JobRequirementResponse.from(savedRequirement);
    }

    @Transactional
    public JobRequirementResponse updateJobRequirements(JobRequirementRequest request,
                                                        Long jobId, Long requirementId){
        authorizeAccess(jobId);
        JobRequirement requirement=findOrThrow(requirementId,jobId);
        requirement.setContent(request.content());
        requirement.setRequirementType(request.requirementType());
        requirement.setDisplayOrder(request.position());
        return JobRequirementResponse.from(requirement);

    }

    @Transactional
    public JobRequirementResponse patchJobRequirement(Long jobId,
                                                      Long requirementId,
                                                      JobRequirementPatchRequest request){

          authorizeAccess(jobId);
          JobRequirement requirement=findOrThrow(requirementId,jobId);
          if(request.content()!=null){
              requirement.setContent(request.content());
          }

          if(request.requirementType()!=null){
               requirement.setRequirementType(request.requirementType());
          }

          Integer position=request.position();
          Integer oldPosition=requirement.getDisplayOrder();

          if(position!=null &&!oldPosition.equals(position)){
               jobRequirementsRepository.shiftPositionForPatch(jobId,position,oldPosition,requirement.getRequirementType().name());
               requirement.setDisplayOrder(position);
          }

          return JobRequirementResponse.from(requirement);
    }

    @Transactional
    public void deleteRequirement(Long jobId,Long requirementId){
         authorizeAccess(jobId);
         JobRequirement requirement=findOrThrow(requirementId, jobId);
         jobRequirementsRepository.delete(requirement);
    }

    private void authorizeAccess(Long jobId){

        Long currentUserId=securityUtils.getCurrentUser().getId();
        Role role=jobRequirementsRepository.findRoleForJob(jobId,currentUserId).orElse(null);
        if(role==null){
             if(!jobService.existsById(jobId)){
                 throw new ResourceNotFoundException("job not found",jobId);
             }
             throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: you are not a member of the company that posted this job."
            );
        }
        if(role!= Role.ADMIN && role!=Role.RECRUITER){
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: Only a company recruiters and admin can modify job requirements belonging to that company."
            );
        }

    }


    private JobRequirement findOrThrow(Long requirementId,Long jobId){
          return jobRequirementsRepository.findByIdAndJobId(requirementId,jobId).orElseThrow(
                  ()->new ResourceNotFoundException("job requirement",requirementId)
          );
    }


}
