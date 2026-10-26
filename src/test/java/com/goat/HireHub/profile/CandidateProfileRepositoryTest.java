package com.goat.HireHub.profile;

import com.goat.HireHub.PostgresTestBase;
import com.goat.HireHub.TestData;
import com.goat.HireHub.education.Education;
import com.goat.HireHub.education.EducationRepository;
import com.goat.HireHub.experience.Experience;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    //experience and education

    @Test
    void experiencesOfOneProfile_dontLeakIntoAnother() {
        CandidateProfile kim = profileFor("kim@x.com");
        CandidateProfile amina = profileFor("amina@x.com");
        experienceRepository.save(experience(kim, "Backend Dev"));
        experienceRepository.save(experience(kim, "Intern"));
        experienceRepository.save(experience(amina, "Designer"));
        em.flush();
        em.clear();

        List<Experience> kims = experienceRepository.findByCandidateProfileId(kim.getId());
        assertEquals(2, kims.size());
        assertTrue(kims.stream().noneMatch(e -> e.getJobTitle().equals("Designer")));
        assertEquals(1, experienceRepository.findByCandidateProfileId(amina.getId()).size());
    }

    @Test
    void educationsOfOneProfile_dontLeakIntoAnother() {
        CandidateProfile kim = profileFor("kim@x.com");
        CandidateProfile amina = profileFor("amina@x.com");
        educationRepository.save(education(kim, "University of Nairobi"));
        educationRepository.save(education(amina, "Strathmore"));
        em.flush();
        em.clear();

        List<Education> kims = educationRepository.findByCandidateProfileId(kim.getId());

        assertEquals(1, kims.size());
        assertEquals("University of Nairobi", kims.get(0).getInstitutionName());
    }

    @Test
    void profileWithNoExperiences_returnsAnEmptyList() {
        CandidateProfile kim = profileFor("kim@x.com");

        assertTrue(experienceRepository.findByCandidateProfileId(kim.getId()).isEmpty());
    }

    @Test
    void experience_withoutTitleOrStartDate_isRejectedByTheDatabase() {
        CandidateProfile kim = profileFor("kim@x.com");
        Experience noTitle = experience(kim, null);

        assertThrows(DataIntegrityViolationException.class, () -> experienceRepository.saveAndFlush(noTitle));
    }

    @Test
    void education_withoutInstitution_isRejectedByTheDatabase() {
        CandidateProfile kim = profileFor("kim@x.com");
        Education noInstitution = education(kim, null);

        assertThrows(DataIntegrityViolationException.class, () -> educationRepository.saveAndFlush(noInstitution));
    }




    //helpers
    private CandidateProfile profileFor(String email) {
        User user = userRepository.saveAndFlush(TestData.user(email));
        return profileRepository.saveAndFlush(
                new CandidateProfile(user, "Headline", "Summary", SearchStatus.OPEN_TO_WORK));
    }

    private Experience experience(CandidateProfile profile, String title) {
        Experience e = new Experience();
        e.setCandidateProfile(profile);
        e.setJobTitle(title);
        e.setCompanyNameRaw("Some Company");
        e.setStartDate(LocalDate.of(2021, 1, 1));
        return e;
    }

    private Education education(CandidateProfile profile, String institution) {
        Education e = new Education();
        e.setCandidateProfile(profile);
        e.setInstitutionName(institution);
        e.setStartDate(LocalDate.of(2016, 9, 1));
        return e;
    }

}
