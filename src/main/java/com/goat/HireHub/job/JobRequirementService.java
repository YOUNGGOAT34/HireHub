package com.goat.HireHub.job;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyMembership;
import com.goat.HireHub.company.CompanyService;
import com.goat.HireHub.company.Role;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.dto.JobResponse;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobRequirementService {
    private final JobRequirementsRepository jobRequirementsRepository;
    private final SecurityUtils securityUtils;
    private final JobService jobService;
    private final CompanyService companyService;

    public JobRequirementService(JobRequirementsRepository jobRequirementsRepository,
                                 SecurityUtils securityUtils,
                                 JobService jobService,
                                 CompanyService companyService
                                 ){
         this.jobRequirementsRepository=jobRequirementsRepository;
         this.securityUtils=securityUtils;
         this.jobService=jobService;
         this.companyService=companyService;
    }

    @Transactional(readOnly = true)
    public List<JobRequirementResponse> getJobRequirements(Long id){
         return jobRequirementsRepository.findByJobIdOrderByDisplayOrderAsc(id)
                 .stream().map(JobRequirementResponse::from)
                 .toList();
    }

    @Transactional
    public JobRequirementResponse addJobRequirement(Long jobId, JobRequirementRequest request){
          Job job=jobService.findOrThrow(jobId);
          authorizeAccess(job);
          JobRequirement requirement=new JobRequirement();
          requirement.setJob(job);
          requirement.setRequirementType(request.requirementType());
          requirement.setContent(request.content());
          requirement.setDisplayOrder(request.position());
          job.addRequirement(requirement);
          return JobRequirementResponse.from(requirement);
    }

    @Transactional
    public JobRequirementResponse updateJobRequirements(JobRequirementRequest request,
                                                        Long jobId, Long requirementId){
        JobRequirement requirement=findOrThrow(requirementId);
        authorizeAccess(jobId);
        requirement.setContent(request.content());
        requirement.setRequirementType(request.requirementType());
        requirement.setDisplayOrder(request.position());
        return JobRequirementResponse.from(requirement);

    }

    private void authorizeAccess(Long jobId){
        JobResponse response=jobService.getById(jobId);
        Long companyId=response.companyId();
        Company company=companyService.findOrThrow(companyId);
        User currentUser=securityUtils.getCurrentUser();
        CompanyMembership membership=companyService.findMemberShipOrThrow(currentUser.getId(),companyId);
        if(membership.getRole() != Role.ADMIN && membership.getRole()!=Role.RECRUITER){
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: Only a company recruiters and admin can modify job requirements belonging to that company."
            );
        }

    }

    private void authorizeAccess(Job job){
        Long companyId=job.getCompany().getId();
        Company company=companyService.findOrThrow(companyId);
        User currentUser=securityUtils.getCurrentUser();
        CompanyMembership membership=companyService.findMemberShipOrThrow(currentUser.getId(),companyId);
        if(membership.getRole() != Role.ADMIN && membership.getRole()!=Role.RECRUITER){
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: Only a company recruiters and admin can modify job requirements belonging to that company."
            );
        }

    }


    private JobRequirement findOrThrow(Long requirementId){
          return jobRequirementsRepository.findById(requirementId).orElseThrow(
                  ()->new ResourceNotFoundException("job requirement")
          );
    }

}
