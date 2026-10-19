package com.goat.HireHub.job;

import com.goat.HireHub.company.*;
import com.goat.HireHub.company.dto.CompanyResponse;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.dto.JobPatchRequest;
import com.goat.HireHub.job.dto.JobRequest;
import com.goat.HireHub.job.dto.JobResponse;
import com.goat.HireHub.job.dto.JobUpdateRequest;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
public class JobService {

    private final JobRepository jobRepository;
    private final SecurityUtils securityUtils;
    private final CompanyService companyService;


    public JobService(JobRepository jobRep,
                      SecurityUtils securityUtils,
                      CompanyService companyService){
         this.jobRepository=jobRep;
         this.securityUtils=securityUtils;
         this.companyService=companyService;
    }

    public Page<JobResponse> getAll(Pageable pageable){

        return jobRepository.findAll(pageable).map(JobResponse::from);
    }

    public Page<JobResponse> searchJobs(
            String title,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String companyName,
            Long companyId,
            Pageable pageable
          ){

        Specification<Job> jobSpecification=JobSpecification
                .titleContains(title)
                .and(JobSpecification.salaryAtLeast(minSalary))
                .and(JobSpecification.byCompany(companyName))
                .and(JobSpecification.salaryAtMost(maxSalary))
                .and(JobSpecification.byCompanyId(companyId));

        return jobRepository.findAll(jobSpecification,pageable).map(JobResponse::from);


    }

    @Transactional
    public JobResponse addJob(JobRequest request){
        Company company=authorizeAccess(request.companyId());
        Job job=new Job();
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setSalary(request.salary());
        job.setCompany(company);
        return JobResponse.from(jobRepository.save(job));
    }



    public JobResponse getById(Long id){
            return JobResponse.from(findOrThrow(id));
    }

    @Transactional
    public void deleteById(Long id){
         Job savedJob=findOrThrow(id);
         authorizeAccess(savedJob.getCompany().getId());
        jobRepository.delete(savedJob);
    }

    @Transactional
    public JobResponse updateJob(Long id, JobUpdateRequest updatedJob){

        Job savedJob=findOrThrow(id);
        authorizeAccess(savedJob.getCompany().getId());
        savedJob.setDescription(updatedJob.description());
        savedJob.setTitle(updatedJob.title());
        savedJob.setSalary(updatedJob.salary());

        return JobResponse.from(savedJob);
    }

    @Transactional
    public JobResponse patchJob(Long id, JobPatchRequest updatedJob){

        Job savedJob=findOrThrow(id);
        authorizeAccess(savedJob.getCompany().getId());
        if(updatedJob.salary()!=null){
            savedJob.setSalary((updatedJob.salary()));
        }

        if(updatedJob.title()!=null){
            savedJob.setTitle(updatedJob.title());
        }

        if(updatedJob.description()!=null){
            savedJob.setDescription(updatedJob.description());
        }

        return JobResponse.from(savedJob);
    }


    private Company authorizeAccess(Long id){
        Company company=companyService.findOrThrow(id);
        User currentUser=securityUtils.getCurrentUser();
        CompanyMembership membership=companyService.findMemberShipOrThrow(currentUser.getId(), id);
        if(membership.getRole() != Role.ADMIN && membership.getRole()!=Role.RECRUITER){
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: Only a company recruiters and admin can post or modify jobs belonging to that company."
            );
        }

        return company;
    }

    private Job findOrThrow(Long id){
        return jobRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("job",id));
    }

}

