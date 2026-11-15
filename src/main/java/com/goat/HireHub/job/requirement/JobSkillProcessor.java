package com.goat.HireHub.job.requirement;

import com.goat.HireHub.job.Job;
import com.goat.HireHub.job.JobService;
import com.goat.HireHub.job.gemini.GeminiService;
import com.goat.HireHub.job.gemini.RequirementExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@Service
public class JobSkillProcessor {

    private static final Logger log = LoggerFactory.getLogger(JobSkillProcessor.class);

    private record Snapshot(String text, Instant staleSince) {}

    private final RequirementExtractor extractor;
    private final GeminiService gemini;
    private final JobService jobService;
    private final JobRequirementsRepository repository;
    private final TransactionTemplate tx;

    public JobSkillProcessor(GeminiService geminiService,
                             JobRequirementsRepository repository,
                             JobService jobService,
                             RequirementExtractor extractor,
                             TransactionTemplate tx
                           ){
         this.gemini=geminiService;
         this.repository=repository;
         this.jobService=jobService;
         this.extractor=extractor;
         this.tx=tx;

    }

    public void processJob(Long jobId) {
        try {

            Snapshot snap = tx.execute(status -> {
                Job job = jobService.findOrThrow(jobId);
                Instant seen = job.getSkillsStaleSince();
                if (seen == null) return null;
                return new Snapshot(extractor.extractJobRequirements(job), seen);
            });
            if (snap == null) return;

            List<String> names = (snap.text() == null) ? List.of() : gemini.getSkills(snap.text());

            tx.executeWithoutResult(status -> {
                jobService.saveJobSkills(jobId,names,snap.staleSince);

            });

        } catch (Exception e) {
            log.warn("Skill extraction failed for job {}", jobId, e);
            tx.executeWithoutResult(status -> repository.recordFailure(jobId));
        }
    }
}
