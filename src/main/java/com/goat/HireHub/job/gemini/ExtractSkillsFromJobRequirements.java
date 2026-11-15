package com.goat.HireHub.job.gemini;

import com.goat.HireHub.job.Job;
import com.goat.HireHub.job.JobService;
import com.goat.HireHub.job.requirement.JobRequirement;
import com.goat.HireHub.job.requirement.RequirementType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ExtractSkillsFromJobRequirements {

    private final JobService jobService;

    public ExtractSkillsFromJobRequirements(JobService jobService){
           this.jobService=jobService;
    }

    @Transactional(readOnly = true)
    public String extractJobRequirements(Long jobId){
            Job job=jobService.findOrThrow(jobId);
            StringBuilder requirements=new StringBuilder();
            requirements.append("job title: ").append(job.getTitle()).append("\n");

        Map<RequirementType, List<String>> requirementsByType=new EnumMap<>(RequirementType.class);

        for (JobRequirement requirement :job.getRequirements()){
             requirementsByType.computeIfAbsent(requirement.getRequirementType(),
                     k->new ArrayList<>()).add(requirement.getContent());
        }

        appendSection(requirements,"Responsibilities",requirementsByType.get(RequirementType.RESPONSIBILITY));
        appendSection(requirements,"Qualifications(required)",requirementsByType.get(RequirementType.QUALIFICATION));
        appendSection(requirements,"Preferred Qualifications",requirementsByType.get(RequirementType.PREFERRED_QUALIFICATION));
        appendSection(requirements,"Nice To Have",requirementsByType.get(RequirementType.NICE_TO_HAVE));

        return requirements.toString();
    }

    private void appendSection(StringBuilder builder,String title,List<String> items){
          if(items==null || items.isEmpty()) return;
          builder.append("\n").append(title).append(":\n");
          for(String item:items){
              builder.append("-").append(item).append("\n");
          }
    }
}
