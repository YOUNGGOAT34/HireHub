package com.goat.HireHub.job;

import com.goat.HireHub.PostgresTestBase;
import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class JobRepositoryTest extends PostgresTestBase {
    @Autowired
    JobRepository jobRepository;
    @Autowired
    CompanyRepository companyRepository;

    Company safaricom;
    Company equity;

    @BeforeEach
    void seed() {
        safaricom = companyRepository.save(company("Safaricom"));
        equity = companyRepository.save(company("Equity"));

        List<Job> jobs = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            String title = (i % 2 == 0 ? "Java Developer " : "Python Developer ") + i;
            jobs.add(job(title, i * 1000, i <= 15 ? safaricom : equity));
        }
        jobRepository.saveAll(jobs);
        jobRepository.flush();
    }

    //filtering
    @Test
    void titleFilter_isCaseInsensitiveAndMatchesPartialText() {
        Page<Job> result = jobRepository.findAll(
                JobSpecification.titleContains("jAvA"), PageRequest.of(0, 50));

        assertEquals(12, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(j -> j.getTitle().startsWith("Java")));
    }

    @Test
    void salaryRange_isInclusiveOnBothEnds() {
        Specification<Job> spec = JobSpecification.salaryAtLeast(new BigDecimal("5000"))
                .and(JobSpecification.salaryAtMost(new BigDecimal("8000")));

        Page<Job> result = jobRepository.findAll(spec, PageRequest.of(0, 50, Sort.by("salary")));

        // Print general pagination details
        System.out.println("Total elements found: " + result.getTotalElements());

        // Print every job matching the specification
        System.out.println("--- Retrieved Jobs ---");
        result.getContent().forEach(job -> {
            System.out.println("Job ID: " + job.getId() + " | Title: " + job.getTitle() + " | Salary: " + job.getSalary());
        });

        assertEquals(4, result.getTotalElements());   // 5000, 6000, 7000, 8000
        assertEquals(0, new BigDecimal("5000").compareTo(result.getContent().get(0).getSalary()));
        assertEquals(0, new BigDecimal("8000").compareTo(result.getContent().get(3).getSalary()));
    }

    @Test
    void companyIdFilter_returnsOnlyThatCompanysJobs() {
        Page<Job> result = jobRepository.findAll(
                JobSpecification.byCompanyId(equity.getId()), PageRequest.of(0, 50));

        assertEquals(10, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(j -> j.getCompany().getId().equals(equity.getId())));
    }

    @Test
    void companyNameFilter_returnsOnlyThatCompanysJobs() {
        Page<Job> result = jobRepository.findAll(
                JobSpecification.byCompany("safaricom"), PageRequest.of(0, 50));

        assertEquals(15, result.getTotalElements());
    }

    @Test
    void noFilters_returnsEveryJob() {
        Specification<Job> spec = JobSpecification.titleContains(null)
                .and(JobSpecification.salaryAtLeast(null))
                .and(JobSpecification.salaryAtMost(null))
                .and(JobSpecification.byCompany(null))
                .and(JobSpecification.byCompanyId(null));

        assertEquals(25, jobRepository.findAll(spec, PageRequest.of(0, 50)).getTotalElements());
    }

    @Test
    void noMatches_returnsEmptyPageWithZeroTotals() {
        Page<Job> result = jobRepository.findAll(
                JobSpecification.titleContains("rust"), PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    //filtering + sorting +pagination combined

    @Test
    void javaJobsAbove10k_sortedBySalaryDesc_firstPage() {
        Specification<Job> spec = JobSpecification.titleContains("java")
                .and(JobSpecification.salaryAtLeast(new BigDecimal("10000")));

        Page<Job> page = jobRepository.findAll(spec,
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "salary")));

        // Java jobs >= 10000: i = 10,12,14,16,18,20,22,24 -> 8 rows
        assertEquals(8, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertEquals(List.of(24000, 22000, 20000), salaries(page));
        assertTrue(page.isFirst());
    }

    @Test
    void javaJobsAbove10k_sortedBySalaryDesc_lastPageHasTheRemainder() {
        Specification<Job> spec = JobSpecification.titleContains("java")
                .and(JobSpecification.salaryAtLeast(new BigDecimal("10000")));

        Page<Job> page = jobRepository.findAll(spec,
                PageRequest.of(2, 3, Sort.by(Sort.Direction.DESC, "salary")));

        assertEquals(List.of(12000, 10000), salaries(page));
        assertTrue(page.isLast());
    }

    @Test
    void pages_doNotOverlap_andTogetherCoverEveryRow() {
        Sort sort = Sort.by(Sort.Direction.ASC, "salary");
        Set<Long> seen = new HashSet<>();

        for (int p = 0; p < 3; p++) {
            Page<Job> page = jobRepository.findAll(Specification.unrestricted(), PageRequest.of(p, 10, sort));
            for (Job j : page.getContent()) {
                assertTrue(seen.add(j.getId()), "job " + j.getId() + " appeared on two pages");
            }
        }

        assertEquals(25, seen.size());   // pages of 10 + 10 + 5
    }

    @Test
    void sortByTitleAsc_ordersAlphabetically() {
        Page<Job> page = jobRepository.findAll(Specification.unrestricted(),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "title")));

        List<String> titles = page.getContent().stream().map(Job::getTitle).toList();
        List<String> sorted = titles.stream().sorted().toList();
        assertEquals(sorted, titles);
    }

    //stable paging when the sort column has duplicates
    @Test
    void sortingBySalaryWithTies_andIdAsTieBreaker_neverRepeatsOrLosesRows() {
        List<Job> ties = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            ties.add(job("Tie Job " + i, 99999, safaricom));   // seven jobs, identical salary
        }
        jobRepository.saveAll(ties);
        jobRepository.flush();

        Sort sort = Sort.by(Sort.Direction.DESC, "salary").and(Sort.by("id"));
        Specification<Job> spec = JobSpecification.titleContains("tie job");
        Set<Long> seen = new HashSet<>();

        for (int p = 0; p < 3; p++) {   // pages of 3 + 3 + 1
            for (Job j : jobRepository.findAll(spec, PageRequest.of(p, 3, sort)).getContent()) {
                assertTrue(seen.add(j.getId()), "job " + j.getId() + " repeated across pages");
            }
        }

        assertEquals(7, seen.size());
    }


    private Company company(String name) {
        Company c = new Company();
        c.setName(name);
        c.setDescription(name + " description");
        return c;
    }

    private Job job(String title, int salary, Company company) {
        Job j = new Job();
        j.setTitle(title);
        j.setDescription("Description for " + title);
        j.setSalary(new BigDecimal(salary));
        j.setCompany(company);
        return j;
    }

    private List<Integer> salaries(Page<Job> page) {
        return page.getContent().stream().map(j -> j.getSalary().intValue()).toList();
    }


}
