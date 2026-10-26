package com.goat.HireHub.User;

import com.goat.HireHub.PostgresTestBase;
import com.goat.HireHub.TestData;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

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

}
