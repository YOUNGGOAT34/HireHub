package com.goat.HireHub.job.requirement;

import com.goat.HireHub.company.Role;
import com.goat.HireHub.job.RequirementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobRequirementsRepository extends JpaRepository<JobRequirement,Long> {
    List<JobRequirement> findByJobIdOrderByDisplayOrderAsc(Long id);
    Optional<JobRequirement> findByIdAndJobId(Long id, Long jobId);
    @Query(
            value = """
                    SELECT m.role
                    FROM company_memberships m
                    LEFT JOIN jobs j ON j.company_id=m.company_id
                    WHERE j.job_id= :job_id AND m.user_id= :user_id
                    """,
            nativeQuery = true
    )
    Optional<Role> findRoleForJob(@Param("job_id")Long jobId,@Param("user_id")Long userId);
    @Query(value = """
            SELECT coalesce(max(r.display_order),0)+1
            FROM job_requirements r
            WHERE  r.job_id= :job_id AND r.requirement_type= :requirement_type
            """,
    nativeQuery = true)
    int nextPosition(@Param("job_id")Long jobId, @Param("requirement_type")String type);
    @Query(value = """
             UPDATE job_requirements
             SET display_order=display_order+1
             WHERE job_id= :job_id
                    AND requirement_type= :type
                    AND display_order>= :starting_position
            """,
            nativeQuery = true
      )
    void shiftPositionDown(@Param("job_id")Long jobId,@Param("type")String type,@Param("starting_position")Integer startingPosition);
}
