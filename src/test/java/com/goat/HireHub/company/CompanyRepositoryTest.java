package com.goat.HireHub.company;

import com.goat.HireHub.PostgresTestBase;
import com.goat.HireHub.TestData;
import com.goat.HireHub.job.Job;
import com.goat.HireHub.job.JobRepository;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompanyRepositoryTest extends PostgresTestBase {
    @Autowired CompanyRepository companyRepository;
    @Autowired CompanyMembershipRepository membershipRepository;
    @Autowired
    JobRepository jobRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;

    //name search
    private void seedCompanies() {
        companyRepository.save(TestData.company("Safaricom"));
        companyRepository.save(TestData.company("Safe Bank"));
        companyRepository.save(TestData.company("Equity"));
        companyRepository.save(TestData.company("Andela"));
        companyRepository.flush();
    }

    @Test
    void nameContains_isPartialAndCaseInsensitive() {
        seedCompanies();
        Page<Company> page = companyRepository.findAll(
                CompanySpecification.nameContains("SAF"), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void nameContains_withNull_returnsEveryCompany() {
        seedCompanies();

        Page<Company> page = companyRepository.findAll(
                CompanySpecification.nameContains(null), PageRequest.of(0, 10));

        assertEquals(4, page.getTotalElements());
    }

    @Test
    void nameContains_noMatch_returnsEmptyPage() {
        seedCompanies();

        Page<Company> page = companyRepository.findAll(
                CompanySpecification.nameContains("zzz"), PageRequest.of(0, 10));

        assertTrue(page.isEmpty());
    }

    @Test
    void search_sortedByNameDesc_pagesAreOrderedAndDontOverlap() {
        seedCompanies();
        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Specification<Company> all = CompanySpecification.nameContains(null);
        List<String> page0 = companyRepository.findAll(all, PageRequest.of(0, 2, sort))
                .map(Company::getName).getContent();
        List<String> page1 = companyRepository.findAll(all, PageRequest.of(1, 2, sort))
                .map(Company::getName).getContent();

        assertEquals(List.of("Safe Bank", "Safaricom"), page0);
        assertEquals(List.of("Equity", "Andela"), page1);
    }

    //deleting a company
    @Test
    void deletingACompany_removesItsJobsAndMemberships() {
        User owner = userRepository.save(TestData.user("owner@x.com"));
        Company company = companyRepository.save(TestData.company("Safaricom"));
        membershipRepository.save(new CompanyMembership(owner, company, Role.ADMIN));
        jobRepository.save(job("Java Developer", company));
        jobRepository.save(job("Python Developer", company));
        em.flush();
        em.clear();

        companyRepository.deleteById(company.getId());
        em.flush();

        assertFalse(companyRepository.existsById(company.getId()));
        assertEquals(0, jobRepository.count());
        assertEquals(0, membershipRepository.count());
    }

    @Test
    void deletingACompany_doesNotDeleteItsMembersAccounts() {
        User owner = userRepository.save(TestData.user("owner@x.com"));
        Company company = companyRepository.save(TestData.company("Safaricom"));
        membershipRepository.save(new CompanyMembership(owner, company, Role.ADMIN));
        em.flush();
        em.clear();

        companyRepository.deleteById(company.getId());
        em.flush();

        assertTrue(userRepository.existsById(owner.getId()));
    }

    private Job job(String title, Company company) {
        Job j = new Job();
        j.setTitle(title);
        j.setDescription("Description for " + title);
        j.setSalary(new BigDecimal("50000"));
        j.setCompany(company);
        return j;
    }

}
