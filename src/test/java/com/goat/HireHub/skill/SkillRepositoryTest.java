package com.goat.HireHub.skill;

import com.goat.HireHub.PostgresTestBase;
import com.goat.HireHub.TestData;
import com.goat.HireHub.profile.CandidateProfile;
import com.goat.HireHub.profile.CandidateProfileRepository;
import com.goat.HireHub.user.SearchStatus;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SkillRepositoryTest extends PostgresTestBase {
    @Autowired SkillRepository skillRepository;
    @Autowired
    CandidateProfileRepository profileRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;


    //look up by name
    @Test
    void findByNameIgnoreCase_matchesRegardlessOfCase() {
        skillRepository.saveAndFlush(new Skill("Java"));

        assertEquals("Java", skillRepository.findByNameIgnoreCase("jAVA").orElseThrow().getName());
    }

    @Test
    void findByNameIgnoreCase_isEmptyForAnUnknownSkill() {
        skillRepository.saveAndFlush(new Skill("Java"));
        assertTrue(skillRepository.findByNameIgnoreCase("Rust").isEmpty());
    }

    //unique name
    @Test
    void duplicateSkillName_isRejectedByTheDatabase() {
        skillRepository.saveAndFlush(new Skill("Java"));

        assertThrows(DataIntegrityViolationException.class,
                () -> skillRepository.saveAndFlush(new Skill("Java")));
    }

    @Test
    void skillWithoutName_isRejectedByTheDatabase() {
        assertThrows(DataIntegrityViolationException.class,
                () -> skillRepository.saveAndFlush(new Skill((String) null)));
    }

    //skills on one profile
    @Test
    void findAllByProfileId_returnsOnlyThatProfilesSkills() {
        Skill java = skillRepository.save(new Skill("Java"));
        Skill spring = skillRepository.save(new Skill("Spring"));
        Skill python = skillRepository.save(new Skill("Python"));
        CandidateProfile kim = profileFor("kim@x.com");
        CandidateProfile amina = profileFor("amina@x.com");
        kim.getSkills().add(java);
        kim.getSkills().add(spring);
        amina.getSkills().add(python);
        em.flush();
        em.clear();

        List<String> names = skillRepository.findAllByProfileId(kim.getId())
                .stream().map(Skill::getName).sorted().toList();

        assertEquals(List.of("Java", "Spring"), names);
    }

    @Test
    void findAllByProfileId_forAProfileWithNoSkills_isEmpty() {
        CandidateProfile kim = profileFor("kim@x.com");
        skillRepository.saveAndFlush(new Skill("Java"));

        assertTrue(skillRepository.findAllByProfileId(kim.getId()).isEmpty());
    }

    @Test
    void oneSkill_canBelongToManyProfiles() {
        Skill java = skillRepository.save(new Skill("Java"));
        CandidateProfile kim = profileFor("kim@x.com");
        CandidateProfile amina = profileFor("amina@x.com");
        kim.getSkills().add(java);
        amina.getSkills().add(java);
        em.flush();
        em.clear();

        assertEquals(1, skillRepository.findAllByProfileId(kim.getId()).size());
        assertEquals(1, skillRepository.findAllByProfileId(amina.getId()).size());
        assertEquals(1, skillRepository.count());   // still a single row in the shared skills table
    }

    private CandidateProfile profileFor(String email) {
        User user = userRepository.saveAndFlush(TestData.user(email));
        return profileRepository.saveAndFlush(
                new CandidateProfile(user, "Headline", "Summary", SearchStatus.OPEN_TO_WORK));
    }




}
