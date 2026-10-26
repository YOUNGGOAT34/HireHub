package com.goat.HireHub.company;

import com.goat.HireHub.PostgresTestBase;
import com.goat.HireHub.TestData;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompanyMembershipRepositoryTest extends PostgresTestBase {

    @Autowired CompanyMembershipRepository membershipRepository;
    @Autowired CompanyRepository companyRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;

    User u1, u2, u3, u4;
    Company companyA, companyB;

    @BeforeEach
    void seed() {
        u1 = userRepository.save(TestData.user("u1@x.com"));
        u2 = userRepository.save(TestData.user("u2@x.com"));
        u3 = userRepository.save(TestData.user("u3@x.com"));
        u4 = userRepository.save(TestData.user("u4@x.com"));
        companyA = companyRepository.save(TestData.company("Company A"));
        companyB = companyRepository.save(TestData.company("Company B"));

        membershipRepository.save(new CompanyMembership(u1, companyA, Role.ADMIN));
        membershipRepository.save(new CompanyMembership(u2, companyA, Role.RECRUITER));
        membershipRepository.save(new CompanyMembership(u3, companyA, Role.EMPLOYEE));
        membershipRepository.save(new CompanyMembership(u1, companyB, Role.ADMIN));
        em.flush();
        em.clear();
    }

    //find by user id and company id

    @Test
    void findByUserIdAndCompanyId_returnsTheMembershipWithItsRole() {
        CompanyMembership m = membershipRepository
                .findByUserIdAndCompanyId(u2.getId(), companyA.getId()).orElseThrow();

        assertEquals(Role.RECRUITER, m.getRole());
    }

    @Test
    void findByUserIdAndCompanyId_isEmptyForAnOutsider() {
        assertTrue(membershipRepository.findByUserIdAndCompanyId(u4.getId(), companyA.getId()).isEmpty());
    }

    @Test
    void findByUserIdAndCompanyId_isEmptyForTheWrongCompany() {
        // u2 works for A only; this is the "recruiter of A edits B" case from the RBAC table
        assertTrue(membershipRepository.findByUserIdAndCompanyId(u2.getId(), companyB.getId()).isEmpty());
    }

    @Test
    void sameUser_canBelongToTwoCompaniesWithOwnRoles() {
        assertEquals(Role.ADMIN,
                membershipRepository.findByUserIdAndCompanyId(u1.getId(), companyA.getId()).orElseThrow().getRole());
        assertEquals(Role.ADMIN,
                membershipRepository.findByUserIdAndCompanyId(u1.getId(), companyB.getId()).orElseThrow().getRole());
    }

    //unique (user,company)

    @Test
    void duplicateMembership_isRejectedByTheDatabase() {
        User managedU2 = userRepository.findById(u2.getId()).orElseThrow();
        Company managedA = companyRepository.findById(companyA.getId()).orElseThrow();

        assertThrows(DataIntegrityViolationException.class,
                () -> membershipRepository.saveAndFlush(new CompanyMembership(managedU2, managedA, Role.ADMIN)));
    }

    //membership of a company paginated

    @Test
    void findMembershipsByCompanyId_pagesThroughOnlyThatCompanysMembers() {
        Page<CompanyMembership> first = membershipRepository
                .findMembershipsByCompanyId(companyA.getId(), PageRequest.of(0, 2));
        Page<CompanyMembership> second = membershipRepository
                .findMembershipsByCompanyId(companyA.getId(), PageRequest.of(1, 2));

        assertEquals(3, first.getTotalElements());
        assertEquals(2, first.getTotalPages());
        assertEquals(2, first.getContent().size());
        assertEquals(1, second.getContent().size());
        assertTrue(second.isLast());
    }

    @Test
    void findMembershipsByCompanyId_doesNotMixInOtherCompanies() {
        Page<CompanyMembership> page = membershipRepository
                .findMembershipsByCompanyId(companyB.getId(), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
    }

    @Test
    void findMembershipsByCompanyId_forACompanyWithNoMembers_isEmpty() {
        Company empty = companyRepository.save(TestData.company("Empty Co"));

        Page<CompanyMembership> page = membershipRepository
                .findMembershipsByCompanyId(empty.getId(), PageRequest.of(0, 10));

        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());
    }



}
