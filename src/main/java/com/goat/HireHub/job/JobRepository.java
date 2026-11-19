package com.goat.HireHub.job;
import com.goat.HireHub.job.dto.JobResponseSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {

    @EntityGraph(attributePaths = {"company"})
    Optional<Job> findWithCompanyById(Long id);
    @Query(
            """
                SELECT new com.goat.HireHub.job.dto.JobResponseSummary(
                       j.id,
                       j.title,
                       j.salary,
                       c.id,
                       c.name,
                       j.location,
                       j.employmentType,
                       j.experienceLevel,
                       j.workPlaceType,
                       j.url,
                       j.createdAt
                   )
                   FROM Job j
                   LEFT JOIN j.company c\s
                   \s"""
    )
    Slice<JobResponseSummary> findAllSummaries(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(value="DELETE FROM job_skills s WHERE s.job_id= :jobId",nativeQuery = true)
    void deleteSkillsByJobId(@Param("jobId")Long jobId);
    @Modifying
    @Query("DELETE FROM Job j WHERE j.id= :id ")
    void deleteJobById(@Param("id")Long id);
    @Query(value="SELECT company_id FROM jobs WHERE job_id= :id",nativeQuery = true)
    Optional<Long> findCompanyIdByJobId(@Param("id")Long jobId);
    @Modifying
    @Query(
            value= """
                     UPDATE jobs
                     SET salary=COALESCE( :salary,salary),
                         job_title=COALESCE( :title,job_title),
                         job_description=COALESCE( :description,job_description),
                         job_url=COALESCE( :url,job_url),
                         location=COALESCE( :location,location),
                         workplace_type=COALESCE( :workplaceType,workplace_type),
                         employment_type=COALESCE( :employmentType,employment_type),
                         experience_level=COALESCE( :experienceLevel,experience_level)    \s
                     WHERE job_id= :jobId
                     AND(
                          salary IS DISTINCT FROM COALESCE( :salary,salary)
                          OR job_title IS DISTINCT FROM COALESCE( :title,job_title)
                          OR job_description IS DISTINCT FROM COALESCE( :description,job_description)
                          OR job_url IS DISTINCT FROM COALESCE( :url,job_url)
                          OR location IS DISTINCT FROM COALESCE( :location,location)
                          OR workplace_type IS DISTINCT FROM COALESCE( :workplaceType,workplace_type)
                          OR employment_type IS DISTINCT FROM COALESCE( :employmentType,employment_type)
                          OR experience_level IS DISTINCT FROM COALESCE( :experienceLevel,experience_level)
                     )
                   \s""",
            nativeQuery = true
    )
    void patchJob(@Param("jobId")Long jobId,
                  @Param("salary")BigDecimal salary,
                  @Param("title")String title,
                  @Param("description")String description,
                  @Param("url") String url,
                  @Param("location") String location,
                  @Param("workplaceType")String workplaceType,
                  @Param("employmentType") String employmentType,
                  @Param("experienceLevel")String experienceLevel
                     );

    @Modifying
    @Query(
            value= """
                     UPDATE jobs
                     SET salary= :salary,
                         job_title= :title,
                         job_description= :description,
                         job_url= :url,
                         location= :location,
                         workplace_type= :workplaceType,
                         employment_type= :employmentType,
                         experience_level= :experienceLevel    \s
                     WHERE job_id= :jobId
                     AND(
                          salary IS DISTINCT FROM  :salary
                          OR job_title IS DISTINCT FROM  :title
                          OR job_description IS DISTINCT FROM  :description
                          OR job_url IS DISTINCT FROM  :url
                          OR location IS DISTINCT FROM  :location
                          OR workplace_type IS DISTINCT FROM  :workplaceType
                          OR employment_type IS DISTINCT FROM  :employmentType
                          OR experience_level IS DISTINCT FROM  :experienceLevel
                     )
                   \s""",
            nativeQuery = true
    )
    void updateJob(@Param("jobId")Long jobId,
                  @Param("salary")BigDecimal salary,
                  @Param("title")String title,
                  @Param("description")String description,
                  @Param("url") String url,
                  @Param("location") String location,
                  @Param("workplaceType")String workplaceType,
                  @Param("employmentType") String employmentType,
                  @Param("experienceLevel")String experienceLevel
    );

    @Modifying
    @Query(
            value= """
                      INSERT INTO job_skills(job_id,skill_id)
                      SELECT :jobId,s.skill_id FROM skills s
                      WHERE LOWER(S.name)=ANY(CAST( :names AS text[]))
                      ON CONFLICT DO NOTHING
                    """,
            nativeQuery = true
    )
    void linkSkillsToJob(@Param("jobId")Long jobId,@Param("names")String[] skillNames);
    @Query(
            value= """
                     DELETE FROM job_skills js
                     WHERE js.job_id= :jobId
                     AND NOT EXISTS(
                        SELECT 1 FROM skills s
                        WHERE s.skill_id=js.skill_id
                        AND LOWER(s.skill_name)=ANY(CAST( :names AS text[]))
                     )
                    \s
                   \s""",
            nativeQuery = true

    )
    void unlinkSkillsFromJob(@Param("jobId")Long jobId,@Param("names") String[] names);
}
