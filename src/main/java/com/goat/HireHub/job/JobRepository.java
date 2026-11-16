package com.goat.HireHub.job;
import com.goat.HireHub.job.dto.JobResponseSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {
    @Override
    @EntityGraph(attributePaths = {"company"})
    Optional<Job> findById(Long id);
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

}
