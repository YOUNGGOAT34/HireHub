package com.goat.HireHub.job;

import com.goat.HireHub.company.*;
import com.goat.HireHub.exception.BadRequestException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.dto.*;
import com.goat.HireHub.job.requirement.JobRequirementsRepository;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.skill.Skill;
import com.goat.HireHub.skill.SkillRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


@Service
@Transactional(readOnly = true)
public class JobService {

    private final JobRepository jobRepository;
    private final SecurityUtils securityUtils;
    private final CompanyService companyService;
    private final JobRequirementsRepository requirementsRepository;
    private final SkillRepository skillRepository;

    public JobService(JobRepository jobRep,
                      SecurityUtils securityUtils,
                      CompanyService companyService,
                      SkillRepository skillRepository,
                      JobRequirementsRepository requirementsRepository
                      ){
         this.jobRepository=jobRep;
         this.securityUtils=securityUtils;
         this.companyService=companyService;
        this.requirementsRepository=requirementsRepository;
        this.skillRepository=skillRepository;
    }

    @Cacheable(value = "jobs", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Slice<JobResponseSummary> getAll(Pageable pageable){
        return jobRepository.findAllSummaries(pageable);
    }

    @Cacheable(value="jobSearch",key= "{#title?.trim()?.toLowerCase()?:''," +
            "#minSalary?.stripTrailingZeros(),#maxSalary?.stripTrailingZeros(),#companyName?.trim()?.toLowerCase()?:'',#location?.trim()?.toLowerCase()?:''," +
            "#companyId,#workPlaceType,#experienceLevel,#employmentType,#pageable.pageNumber,#pageable.pageSize,#pageable.sort" +
            "}")
    public Slice<JobResponseSummary> searchJobs(
            String title,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String companyName,
            String location,
            Long companyId,
            WorkPlaceType workPlaceType,
            ExperienceLevel experienceLevel,
            EmploymentType employmentType,
            Pageable pageable
          ){

        Specification<Job> jobSpecification=JobSpecification.withFilters(
                    title,
                minSalary,
                maxSalary,
                location,
                companyName,
                companyId,
                workPlaceType,
                experienceLevel,
                employmentType
        );

        return jobRepository.findBy(jobSpecification,
                     query->query
                             .as(JobResponseSummary.class)
                             .slice(pageable)
                );
    }

    @Cacheable(value="job",key="#id")
    public JobResponse getById(Long id){
            return JobResponse.from(findWithCompanyOrThrow(id));
    }

    @Transactional
    @CacheEvict(value="job",key="#id")
    public void deleteById(Long id){
         Long companyId=jobRepository.findCompanyIdByJobId(id).orElseThrow(
                 ()->new ResourceNotFoundException("job",id)
         );
         authorizeAccess(companyId);
         requirementsRepository.deleteAllByJobId(id);
        jobRepository.deleteJobById(id);
    }
    @Transactional
    public JobResponse addJob(JobRequest request){
        Company company=companyService.findOrThrow(request.companyId());
        authorizeAccess(request.companyId());
        Job job=new Job();
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setSalary(request.salary());
        job.setCompany(company);
        job.setLocation(request.location());
        job.setUrl(request.url());
        job.setWorkPlaceType(request.workPlaceType());
        job.setEmploymentType(request.employmentType());
        job.setExperienceLevel(request.experienceLevel());
        return JobResponse.from(jobRepository.save(job));
    }

    @Transactional
    @CacheEvict(value="job",key="#id")
    public void updateJob(Long id, JobUpdateRequest updatedJob){
        Long companyId=jobRepository.findCompanyIdByJobId(id)
                .orElseThrow(()->new ResourceNotFoundException("job",id));
        authorizeAccess(companyId);
        jobRepository.updateJob(id,updatedJob.salary(),
                updatedJob.title(),
                updatedJob.description(),
                updatedJob.url(),
                updatedJob.location(),
                updatedJob.workPlaceType().name(),
                updatedJob.employmentType().name(),
                updatedJob.experienceLevel().name()
        );
    }

    @Transactional
    @CacheEvict(value="job",key="#id")
    public void patchJob(Long id, JobPatchRequest updatedJob){
        if(updatedJob.salary()==null && updatedJob.title()==null
                && updatedJob.description()==null
                && updatedJob.url()==null
                &&updatedJob.location()==null
                && updatedJob.workPlaceType()==null
                && updatedJob.employmentType()==null
                && updatedJob.experienceLevel()==null
        ) throw new BadRequestException("empty patch body");

        Long companyId=jobRepository.findCompanyIdByJobId(id)
                        .orElseThrow(()->new ResourceNotFoundException("job",id));

        authorizeAccess(companyId);
        jobRepository.patchJob(id,updatedJob.salary(),
                     updatedJob.title(),
                updatedJob.description(),
                updatedJob.url(),
                updatedJob.location(),
                updatedJob.workPlaceType()==null?null:updatedJob.workPlaceType().name(),
                updatedJob.employmentType()==null?null:updatedJob.employmentType().name(),
                updatedJob.experienceLevel()==null?null:updatedJob.experienceLevel().name()
                );
    }


    private void authorizeAccess(Long companyId){
        Long currentUserId=securityUtils.getCurrentUser().id();
        String role=companyService.findRole(currentUserId, companyId);
        if(role.equals("NONE")){
             throw new org.springframework.security.access.AccessDeniedException(
                     "Access Denied: You do not work for this company.");
        }
        if(!role.equals(Role.ADMIN.name()) && !role.equals(Role.RECRUITER.name())){
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: Only a company recruiters and admin can post or modify jobs belonging to that company."
            );
        }
    }
    @Transactional
    public void saveJobSkills(Long jobId, List<String> rawSkills, Instant seenStaleInstanceSince){

        Set<String> uniqueSkills=new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for(String raw:rawSkills){
            if(raw==null) continue;
            String skill=raw.trim();
            if(!skill.isEmpty() && skill.length()<=100){
                uniqueSkills.add(skill);
            }
        }

        String[] names=uniqueSkills.toArray(String[]::new);
        String[] lower=uniqueSkills.stream().map(String::toLowerCase).toArray(String[]::new);
        if(names.length>0){
           skillRepository.insertMissingSkills(names);
           jobRepository.linkSkillsToJob(jobId,lower);
        }
        jobRepository.unlinkSkillsFromJob(jobId,lower);
        requirementsRepository.clearStale(jobId,seenStaleInstanceSince);
    }

    public Job findOrThrow(Long id){
        return jobRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("job",id));
    }

    //if access to any field of the company is needed
    public Job findWithCompanyOrThrow(Long id){
        return jobRepository.findWithCompanyById(id)
                .orElseThrow(()->new ResourceNotFoundException("job",id));
    }

    public boolean existsById(Long jobId){
         return jobRepository.existsById(jobId);
    }

}

