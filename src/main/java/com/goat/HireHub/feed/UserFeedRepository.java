package com.goat.HireHub.feed;

import com.goat.HireHub.feed.dto.SkillBreakDown;
import com.goat.HireHub.feed.dto.UserFeed;
import com.goat.HireHub.job.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFeedRepository extends JpaRepository<Job, Long> {
    @Query(value= """
            with my_skills As(
                select * from profile_skills where profile_id= :profileId
            ),
            matches AS(
              select js.job_id,count(*) AS matched
              from job_skills js
              join my_skills ms on ms.skill_id=js.skill_id
              group by js.job_id
              having count(*)>=2
            )
            
            SELECT
                j.job_id AS jobId,
                j.job_title AS jobTitle,
                j.salary AS jobSalary,
                j.company_id AS companyId,
                c.company_name AS companyName,
                m.matched::numeric/r.required As score
           
            from matches m
            join jobs j on j.job_id=m.job_id
            join companies c on c.company_id=j.company_id
            join lateral(
            select count(*) AS required from job_skills
            where job_id=m.job_id
            )r on true
            order by score Desc, m.matched Desc, j.job_id""",
            countQuery = """
            WITH my_skills AS (
                SELECT skill_id FROM profile_skills WHERE profile_id = :profileId
            )
            SELECT count(*) from(
            SELECT js.job_id
            FROM job_skills js
            JOIN my_skills ms ON ms.skill_id = js.skill_id
            GROUP BY js.job_id
            HAVING count(*) >= 2
            ) t
            """,
            nativeQuery = true)
    Page<UserFeed> getRecommendedJobs(
            Pageable pageable,
            @Param("profileId")
            Long profileId);

    @Query(value= """
             with my_skills AS(
                 select skill_id from profile_skills where profile_id= :profileId
             )
             SELECT
                     js.job_id as jobId,
                     COALESCE(STRING_AGG(CASE WHEN ms.skill_id IS NOT NULL THEN s.skill_name END,','),'') As matching,
                     COALESCE(STRING_AGG(CASE WHEN ms.skill_id IS NULL THEN s.skill_name END,','),'') As missing
            
             from job_skills as js
             join skills s on s.skill_id=js.skill_id
             left join my_skills ms on ms.skill_id=s.skill_id
             where js.job_id in (:jobIds)
             group by js.job_id;
            """,nativeQuery = true)
    List<SkillBreakDown> getSkillBreakdown(
            @Param("jobIds") List<Long> jobIds,
            @Param("profileId") Long profileId);
}
