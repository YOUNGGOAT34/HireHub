
package com.goat.HireHub.config;

import com.goat.HireHub.job.Job;
import com.goat.HireHub.job.JobRepository;
import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class JobDataSeeder {

    @Bean
    CommandLineRunner seedJobs(
            JobRepository jobRepository,
            CompanyRepository companyRepository) {

        return args -> {
            if (jobRepository.count() > 0) {
                System.out.println("Jobs already exist. Skipping seeding.");
                return;
            }

            Company company = companyRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Create a company before seeding jobs."
                    ));

            List<Job> jobs = new ArrayList<>();

            for (int i = 1; i <= 100; i++) {
                Job job = new Job(
                        BigDecimal.valueOf(50000 + (i * 1000L)),
                        "Test job description number " + i,
                        "Software Engineer " + i,
                        company
                );

                jobs.add(job);
            }

            jobRepository.saveAll(jobs);

            System.out.println("Inserted " + jobs.size() + " test jobs.");
        };
    }
}

