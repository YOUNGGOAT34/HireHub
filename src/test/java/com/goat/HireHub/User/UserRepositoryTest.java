package com.goat.HireHub.User;

import com.goat.HireHub.PostgresTestBase;
import com.goat.HireHub.TestData;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

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


}
