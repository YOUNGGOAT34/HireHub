package com.goat.HireHub.job;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyMembership;
import com.goat.HireHub.company.CompanyService;
import com.goat.HireHub.company.Role;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.dto.JobResponse;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import jakarta.transaction.Transactional;
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

    public List<JobRequirementRespose> getJobRequirements(Long id){
         return jobRequirementsRepository.findByJobIdOrderByDisplayOrderAsc(id)
                 .stream().map(JobRequirementRespose::from)
                 .toList();
    }

    @Transactional
    public JobRequirementRespose updateJobRequirements(JobRequirementRequest request,
                                                       Long jobId,Long requirementId){
        JobRequirement requirement=findOrThrow(requirementId);
        authorizeAccess(jobId);
        requirement.setContent(request.content());
        requirement.setRequirementType(request.requirementType());
        requirement.setDisplayOrder(request.position());
        return JobRequirementRespose.from(requirement);

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

    private JobRequirement findOrThrow(Long requirementId){
          return jobRequirementsRepository.findById(requirementId).orElseThrow(
                  ()->new ResourceNotFoundException("job requirement")
          );
    }

}
