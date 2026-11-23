package com.goat.HireHub.devTests;

import com.goat.HireHub.security.AuthenticatedUser;
import com.goat.HireHub.security.CustomUserDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomUserDetailsTest {
    @Test
    void adaptsUserCorrectly(){
        AuthenticatedUser user=new AuthenticatedUser(1L,
                "alice@example.com",
                "password");
        CustomUserDetails details=new CustomUserDetails(user);
        assertEquals("alice@example.com",details.getUsername());
        assertEquals("password",details.getPassword());
        assertEquals(1L,details.getId());
        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonLocked());


    }
}
