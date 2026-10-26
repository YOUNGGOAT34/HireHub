package com.goat.HireHub;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.user.User;

public final class TestData {

    private TestData() {}

    public static User user(String email) {
        return user(email, "First", "Last", null);
    }

    public static User user(String email, String firstName, String lastName, String username) {
        User u = new User();
        u.setEmail(email);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setUsername(username);
        u.setPasswordHash("hashed-password");
        return u;
    }

    public static Company company(String name) {
        Company c = new Company();
        c.setName(name);
        c.setDescription(name + " description");
        return c;
    }
}
