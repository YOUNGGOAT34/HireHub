package com.goat.HireHub.profile;

import com.goat.HireHub.PostgresTestBase;
import com.goat.HireHub.TestData;
import com.goat.HireHub.education.EducationRepository;
import com.goat.HireHub.experience.ExperienceRepository;
import com.goat.HireHub.skill.SkillRepository;
import com.goat.HireHub.user.SearchStatus;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CandidateProfileRepositoryTest extends PostgresTestBase {
    @Autowired CandidateProfileRepository profileRepository;
    @Autowired
    ExperienceRepository experienceRepository;
    @Autowired
    EducationRepository educationRepository;
    @Autowired
    SkillRepository skillRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;


    //creating a profile

    @Test
    void profileForADetachedUser_savesAndUsesTheUsersIdAsItsPrimaryKey() {

        User user = userRepository.saveAndFlush(TestData.user("kim@x.com"));
        em.clear();   // user is now detached

        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        profile.setHeadline("Software Professional");
        profile.setSearchStatus(SearchStatus.OPEN_TO_WORK);
        CandidateProfile saved = profileRepository.saveAndFlush(profile);

        assertEquals(user.getId(), saved.getId());  // @MapsId: profile id == user id
    }

    @Test
    void savedProfile_canBeReloadedByTheUsersIdWithAllFields() {
        User user = userRepository.saveAndFlush(TestData.user("kim@x.com"));
        profileRepository.saveAndFlush(
                new CandidateProfile(user, "Java Developer", "I build APIs", SearchStatus.OPEN_TO_WORK));
        em.clear();
        CandidateProfile loaded = profileRepository.findById(user.getId()).orElseThrow();
        assertEquals("Java Developer", loaded.getHeadline());
        assertEquals("I build APIs", loaded.getSummary());
        assertEquals(SearchStatus.OPEN_TO_WORK, loaded.getSearchStatus());
    }

    @Test
    void findById_forAUserWithoutAProfile_isEmpty() {
        User user = userRepository.saveAndFlush(TestData.user("kim@x.com"));
        assertTrue(profileRepository.findById(user.getId()).isEmpty());
    }


}
