package com.goat.HireHub.User;

import com.goat.HireHub.PostgresTestBase;
import com.goat.HireHub.TestData;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import com.goat.HireHub.user.UserSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest extends PostgresTestBase {
    @Autowired
    UserRepository userRepository;

    //email look ups
    @Test
    void existsByEmailIgnoreCase_matchesRegardlessOfCase() {
        userRepository.saveAndFlush(TestData.user("kim@x.com"));

        assertTrue(userRepository.existsByEmailIgnoreCase("KIM@X.COM"));
        assertTrue(userRepository.existsByEmailIgnoreCase("kim@x.com"));
    }

    @Test
    void existsByEmailIgnoreCase_isFalseForUnknownEmail() {
        userRepository.saveAndFlush(TestData.user("kim@x.com"));

        assertFalse(userRepository.existsByEmailIgnoreCase("other@x.com"));
    }

    @Test
    void findByEmailIgnoreCase_returnsTheRightUser() {
        userRepository.saveAndFlush(TestData.user("kim@x.com", "Kim", "Otieno", "kim"));
        userRepository.saveAndFlush(TestData.user("amina@x.com", "Amina", "Hassan", "amina"));
        User found = userRepository.findByEmailIgnoreCase("AMINA@x.com").orElseThrow();

        assertEquals("Amina", found.getFirstName());
    }

    @Test
    void findByEmailIgnoreCase_isEmptyWhenMissing() {
        assertTrue(userRepository.findByEmailIgnoreCase("nobody@x.com").isEmpty());
    }

    //database constraints

    @Test
    void duplicateEmail_isRejectedByTheDatabase() {
        userRepository.saveAndFlush(TestData.user("kim@x.com"));

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(TestData.user("kim@x.com")));
    }

    @Test
    void duplicateUsername_isRejectedByTheDatabase() {
        userRepository.saveAndFlush(TestData.user("a@x.com", "A", "A", "same"));
        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(TestData.user("b@x.com", "B", "B", "same")));
    }

    @Test
    void manyUsersWithoutUsername_areAllowed() {
        userRepository.saveAndFlush(TestData.user("a@x.com"));
        userRepository.saveAndFlush(TestData.user("b@x.com"));

        assertEquals(2, userRepository.count());
    }

    @Test
    void missingRequiredFields_areRejectedByTheDatabase() {
        User noFirstName = TestData.user("a@x.com");
        noFirstName.setFirstName(null);

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(noFirstName));
    }

    //search specification: filter+sort+pagination

    private void seedFourUsers() {
        userRepository.save(TestData.user("kim@x.com", "Kim", "Otieno", "kim"));
        userRepository.save(TestData.user("kimb@x.com", "Kimberly", "Mwangi", "kimb"));
        userRepository.save(TestData.user("amina@x.com", "Amina", "Otieno", "amina"));
        userRepository.save(TestData.user("brian@x.com", "Brian", "Kamau", null));
        userRepository.flush();
    }

    private Specification<User> spec(String first, String last, String username) {
        return UserSpecification.firstNameMatches(first)
                .and(UserSpecification.lastNameMatches(last))
                .and(UserSpecification.usernameMatches(username));
    }

    @Test
    void search_byFirstName_isPartialAndCaseInsensitive() {
        seedFourUsers();

        Page<User> page = userRepository.findAll(spec("KIM", null, null), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());   // Kim, Kimberly
    }

    @Test
    void search_byLastName_matchesBothOtienos() {
        seedFourUsers();

        Page<User> page = userRepository.findAll(spec(null, "otieno", null), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void search_combinedFilters_narrowTheResult() {
        seedFourUsers();

        Page<User> page = userRepository.findAll(spec("kim", "otieno", null), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("kim@x.com", page.getContent().get(0).getEmail());
    }

    @Test
    void search_byUsername_ignoresUsersWithoutOne() {
        seedFourUsers();

        Page<User> page = userRepository.findAll(spec(null, null, "amin"), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
    }

    @Test
    void search_withNoFilters_returnsEveryone() {
        seedFourUsers();

        assertEquals(4, userRepository.findAll(spec(null, null, null), PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void search_noMatches_returnsEmptyPage() {
        seedFourUsers();

        Page<User> page = userRepository.findAll(spec("zzz", null, null), PageRequest.of(0, 10));

        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void search_sortedByFirstNameAsc_pagesAreOrderedAndDontOverlap() {
        seedFourUsers();
        Sort sort = Sort.by(Sort.Direction.ASC, "firstName");

        List<String> page0 = userRepository.findAll(spec(null, null, null), PageRequest.of(0, 2, sort))
                .map(User::getFirstName).getContent();
        List<String> page1 = userRepository.findAll(spec(null, null, null), PageRequest.of(1, 2, sort))
                .map(User::getFirstName).getContent();

        assertEquals(List.of("Amina", "Brian"), page0);
        assertEquals(List.of("Kim", "Kimberly"), page1);
    }


}
