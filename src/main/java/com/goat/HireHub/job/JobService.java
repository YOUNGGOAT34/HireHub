package com.goat.HireHub.job;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRep){
         this.jobRepository=jobRep;
    }

    public List<Job> getAll(){
        return jobRepository.findAll();
    }

    public Job addJob(Job job){
        return jobRepository.save(job);
    }

    public Job getById(Long id){
        Optional<Job> job=jobRepository.findById(id);
        return job.orElse(null);
    }

    public boolean deleteById(Long id){
         if(jobRepository.existsById(id)){
              jobRepository.deleteById(id);
              return true;
         }
         return false;
    }

    @Transactional
    public Job updateJob(Long id,Job updatedJob){
        Job savedJob=jobRepository.findById(id)
                .orElseThrow(()->new RuntimeException("job not found with id "+id));

        savedJob.setDescription(updatedJob.getDescription());
        savedJob.setTitle(updatedJob.getTitle());
        savedJob.setSalary(updatedJob.getSalary());

        return savedJob;
    }

    @Transactional
    public Job patchJob(Long id,Job updatedJob){

        Job savedJob=jobRepository.findById(id)
                .orElseThrow(()->new RuntimeException("job not found with id "+id));

        if(updatedJob.getSalary()!=null){
            savedJob.setSalary((updatedJob.getSalary()));
        }

        if(updatedJob.getTitle()!=null){
            savedJob.setTitle(updatedJob.getTitle());
        }

        if(updatedJob.getDescription()!=null){
            savedJob.setDescription(updatedJob.getDescription());
        }

        return savedJob;
    }

}

