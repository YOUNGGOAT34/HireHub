package com.goat.HireHub.job;

import com.goat.HireHub.company.*;
import com.goat.HireHub.company.membership.CompanyMembership;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.dto.*;
import com.goat.HireHub.job.gemini.GeminiService;
import com.goat.HireHub.job.requirement.JobRequirementsRepository;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.skill.Skill;
import com.goat.HireHub.skill.SkillRepository;
import com.goat.HireHub.user.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
                       GeminiService geminiService,
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

    @Cacheable(value="job",key="#id")
    public JobResponse getById(Long id){
            return JobResponse.from(findOrThrow(id));
    }
    @Transactional
    @CacheEvict(value="job",key="#id")
    public void deleteById(Long id){
         Job savedJob=findOrThrow(id);
         authorizeAccess(savedJob.getCompany().getId());
        jobRepository.delete(savedJob);
    }

    @Transactional
    @CachePut(value="job",key="#id")
    public JobResponse updateJob(Long id, JobUpdateRequest updatedJob){

        Job savedJob=findOrThrow(id);
        authorizeAccess(savedJob.getCompany().getId());
        savedJob.setDescription(updatedJob.description());
        savedJob.setTitle(updatedJob.title());
        savedJob.setSalary(updatedJob.salary());
        savedJob.setUrl(updatedJob.url());
        savedJob.setLocation(updatedJob.location());
        savedJob.setWorkPlaceType(updatedJob.workPlaceType());
        savedJob.setExperienceLevel(updatedJob.experienceLevel());
        savedJob.setEmploymentType(updatedJob.employmentType());
        return JobResponse.from(savedJob);
    }

    @Transactional
    @CachePut(value="job",key="#id")
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

        if(updatedJob.url()!=null){
            savedJob.setUrl(updatedJob.url());
        }

        if(updatedJob.location()!=null){
            savedJob.setLocation(updatedJob.location());
        }

        if(updatedJob.workPlaceType()!=null){
            savedJob.setWorkPlaceType(updatedJob.workPlaceType());
        }

        if(updatedJob.employmentType()!=null){
            savedJob.setEmploymentType(updatedJob.employmentType());
        }

        if(updatedJob.experienceLevel()!=null){
            savedJob.setExperienceLevel(updatedJob.experienceLevel());
        }

        return JobResponse.from(savedJob);
    }


    private void authorizeAccess(Long id){
        Long currentUserId=securityUtils.getCurrentUser().id();
        CompanyMembership membership=companyService.findMemberShipOrThrow(currentUserId, id);
        if(membership.getRole() != Role.ADMIN && membership.getRole()!=Role.RECRUITER){
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: Only a company recruiters and admin can post or modify jobs belonging to that company."
            );
        }

    }

    @Transactional
    public void saveJobSkills(Long jobId, List<String> rawSkills, Instant seenStaleInstanceSince){
        Job job=findOrThrow(jobId);
        Set<String> uniqueSkills=new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for(String raw:rawSkills){
            if(raw==null) continue;

            String skill=raw.trim();

            if(!skill.isEmpty() && skill.length()<=100){
                uniqueSkills.add(skill);
            }
        }

        Set<Skill> skills=new HashSet<>();

        for(String name:uniqueSkills){
             Skill skill=skillRepository.findByNameIgnoreCase(name)
                     .orElseGet(()->skillRepository.save(new Skill(name)));
             skills.add(skill);
        }

        job.getJobSkills().clear();
        job.getJobSkills().addAll(skills);
        requirementsRepository.clearStale(jobId,seenStaleInstanceSince);
    }

    public Job findOrThrow(Long id){
        return jobRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("job",id));
    }

    public boolean existsById(Long jobId){
         return jobRepository.existsById(jobId);
    }

}

