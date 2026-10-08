package com.goat.HireHub.job;

import com.goat.HireHub.exception.ResourceNotFoundException;
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
        return jobRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("job",id));
    }

    public void deleteById(Long id){
         if(!jobRepository.existsById(id)){
             throw new ResourceNotFoundException("job",id);
         }
        jobRepository.deleteById(id);
    }

    @Transactional
    public Job updateJob(Long id,Job updatedJob){
        Job savedJob=getById(id);

        savedJob.setDescription(updatedJob.getDescription());
        savedJob.setTitle(updatedJob.getTitle());
        savedJob.setSalary(updatedJob.getSalary());

        return savedJob;
    }

    @Transactional
    public Job patchJob(Long id,Job updatedJob){

        Job savedJob=getById(id);

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

