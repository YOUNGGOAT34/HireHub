package com.goat.HireHub.job;
import com.goat.HireHub.job.dto.JobResponseSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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
    Optional<Long> findCompanyByJobId(@Param("id")Long jobId);
}
