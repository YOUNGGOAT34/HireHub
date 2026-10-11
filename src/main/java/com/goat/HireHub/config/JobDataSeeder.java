
package com.goat.HireHub.config;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyRepository;
import com.goat.HireHub.job.Job;
import com.goat.HireHub.job.JobRepository;

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

            // 1. Define sample companies.
            String[][] sampleCompanies = {
                    {"Google", "Technology and internet services"},
                    {"Microsoft", "Cloud computing and software"},
                    {"Amazon", "E-commerce and cloud computing"},
                    {"Safaricom", "Telecommunications and digital services"},
                    {"Andela", "Technology talent and software development"}
            };

            // 2. Reuse existing companies or create missing ones.
            List<Company> companies = new ArrayList<>(
                    companyRepository.findAll()
            );

            List<Company> sampleCompanyRecords = new ArrayList<>();

            for (String[] data : sampleCompanies) {
                Company company = companies.stream()
                        .filter(c -> c.getName() != null
                                && c.getName().equalsIgnoreCase(data[0]))
                        .findFirst()
                        .orElse(null);

                if (company == null) {
                    company = companyRepository.save(
                            new Company(data[0], data[1])
                    );
                    companies.add(company);
                }

                sampleCompanyRecords.add(company);
            }

            // 3. Don't insert duplicate test jobs on every startup.
            if (jobRepository.count() > 0) {
                System.out.println(
                        "Jobs already exist. Skipping job seeding."
                );
                return;
            }

            // 4. Define varied job titles.
            String[] titles = {
                    "Backend Developer",
                    "Frontend Developer",
                    "Full Stack Engineer",
                    "DevOps Engineer",
                    "Data Engineer",
                    "Machine Learning Engineer",
                    "QA Engineer",
                    "Cybersecurity Analyst",
                    "Database Administrator",
                    "Mobile Developer",
                    "Cloud Engineer",
                    "Software Architect",
                    "Product Manager",
                    "UI/UX Designer",
                    "Technical Support Engineer",
                    "Systems Administrator",
                    "Java Developer",
                    "Python Developer",
                    "Site Reliability Engineer",
                    "Engineering Manager"
            };

            // 5. Create 20 different titles for each company.
            List<Job> jobs = new ArrayList<>();

            for (int companyIndex = 0;
                 companyIndex < sampleCompanyRecords.size();
                 companyIndex++) {

                Company company = sampleCompanyRecords.get(companyIndex);

                for (int titleIndex = 0;
                     titleIndex < titles.length;
                     titleIndex++) {

                    String title = titles[titleIndex];

                    BigDecimal salary = BigDecimal.valueOf(
                            40000L
                                    + (companyIndex * 15000L)
                                    + (titleIndex * 2500L)
                    );

                    String description =
                            "Join " + company.getName()
                                    + " as a " + title
                                    + ". Work with a team to build, maintain, "
                                    + "and improve products and services.";

                    jobs.add(new Job(
                            salary,
                            description,
                            title,
                            company
                    ));
                }
            }

            // 6. Save all 100 jobs.
            jobRepository.saveAll(jobs);

            System.out.println(
                    "Inserted " + jobs.size()
                            + " sample jobs across "
                            + sampleCompanyRecords.size()
                            + " companies."
            );
        };
    }
}