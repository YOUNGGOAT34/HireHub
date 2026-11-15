package com.goat.HireHub.job.gemini;
import com.goat.HireHub.job.requirement.JobRequirementsRepository;
import com.goat.HireHub.job.requirement.JobSkillProcessor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class SkillExtractionWorker {

    private static final long QUIET_PERIOD_SECONDS = 20;
    private static final int BATCH_SIZE = 10;

    private final JobRequirementsRepository jobRequirementsRepository;
    private final JobSkillProcessor processor;

    public SkillExtractionWorker(JobRequirementsRepository requirementsRepository, JobSkillProcessor processor) {
        this.jobRequirementsRepository = requirementsRepository;
        this.processor = processor;
    }

    @Scheduled(fixedDelay = 15_000)
    public void run() {
        Instant cutoff = Instant.now().minusSeconds(QUIET_PERIOD_SECONDS);
        List<Long> due = jobRequirementsRepository.findDueForExtraction(cutoff, PageRequest.of(0, BATCH_SIZE));
        for (Long jobId : due) {
            processor.processJob(jobId);
        }
    }
}