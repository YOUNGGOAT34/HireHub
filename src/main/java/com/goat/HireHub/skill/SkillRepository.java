package com.goat.HireHub.skill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill,Long> {

    Optional<Skill> findByNameIgnoreCase(String name);
    @Query("SELECT s FROM CandidateProfile cp JOIN cp.skills s WHERE cp.id = :profileId")
    List<Skill> findAllByProfileId(@Param("profileId") Long profileId);

    @Modifying
    @Query(
            value = """
                     INSERT INTO skills(name)
                     SELECT n FROM UNNEST(CAST( :names AS text[]) AS n
                     ON CONFLICT(LOWER(name)) DO NOTHING
                    """,
            nativeQuery = true
    )
    void insertMissingSkills(@Param("names")String skillNames);

}
