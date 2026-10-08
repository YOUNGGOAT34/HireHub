package com.goat.HireHub.job;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyRepository;
import com.goat.HireHub.company.dto.CompanyResponse;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.dto.JobPatchRequest;
import com.goat.HireHub.job.dto.JobRequest;
import com.goat.HireHub.job.dto.JobResponse;
import com.goat.HireHub.job.dto.JobUpdateRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobService(JobRepository jobRep,CompanyRepository companyRepository){
         this.jobRepository=jobRep;
         this.companyRepository=companyRepository;
    }

    public List<JobResponse> getAll(){

        return jobRepository.findAll().stream().map(JobResponse::from).toList();
    }

    public JobResponse addJob(JobRequest request){
        Company company=companyRepository.findById(request.companyId())
                .orElseThrow(()->new ResourceNotFoundException("company", request.companyId()));
        Job job=new Job();
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setSalary(request.salary());
        job.setCompany(company);
        return JobResponse.from(jobRepository.save(job));
    }

    private Job findOrThrow(Long id){
        return jobRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("job",id));
    }

    public JobResponse getById(Long id){
            return JobResponse.from(findOrThrow(id));
    }

    public void deleteById(Long id){
         if(!jobRepository.existsById(id)){
             throw new ResourceNotFoundException("job",id);
         }
        jobRepository.deleteById(id);
    }

    @Transactional
    public JobResponse updateJob(Long id, JobUpdateRequest updatedJob){
        Job savedJob=findOrThrow(id);
        savedJob.setDescription(updatedJob.description());
        savedJob.setTitle(updatedJob.title());
        savedJob.setSalary(updatedJob.salary());

        return JobResponse.from(savedJob);
    }

    @Transactional
    public JobResponse patchJob(Long id, JobPatchRequest updatedJob){

        Job savedJob=findOrThrow(id);

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

}

