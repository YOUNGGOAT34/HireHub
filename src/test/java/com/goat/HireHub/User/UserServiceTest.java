package com.goat.HireHub.User;

import com.goat.HireHub.education.EducationRepository;
import com.goat.HireHub.experience.ExperienceRepository;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import profile.CandidateProfile;

import java.util.ArrayList;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ExperienceRepository experienceRepository;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private EducationRepository educationRepository;
    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private CandidateProfile sampleProfile;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setFirstName("John");
        sampleUser.setLastName("Doe");
        sampleUser.setEmail("john.doe@example.com");
        sampleUser.setUsername("johndoe");
        sampleUser.setPasswordHash("encoded_pass");

        sampleProfile = new CandidateProfile();
        sampleProfile.setId(1L);
        sampleProfile.setUser(sampleUser);
        sampleProfile.setHeadline("Software Engineer");
        sampleProfile.setSummary("Java Expert");
        sampleProfile.setSearchStatus(SearchStatus.OPEN_TO_WORK);
        sampleProfile.setExperiences(new ArrayList<>());
        sampleProfile.setEducations(new ArrayList<>());
        sampleProfile.setSkills(new ArrayList<>());

        sampleUser.setCandidateProfile(sampleProfile);
    }
}
