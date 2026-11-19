package com.goat.HireHub.company.membership;

import com.goat.HireHub.company.Role;
import com.goat.HireHub.company.membership.dto.CompanyMemberResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership,Long> {

    @Query(
         value= """
                 SELECT COALESCE(cm.role,'NONE')
                 FROM companies c
                 LEFT JOIN company_memberships cm
                       ON cm.company_id=c.company_id
                       AND cm.user_id= :userId
                 WHERE c.company_id= :companyId
                 """,
            nativeQuery = true
    )
    Optional<String> findRoleByUserIdAndCompanyId(@Param("userId")Long userId,@Param("companyId")Long companyId);
    @Query(value =
            """
             SELECT new com.goat.HireHub.company.membership.dto.CompanyMemberResponse(
                u.id,
                u.firstName,
                u.lastName,
                cm.role
             )
             FROM CompanyMembership cm\s
             JOIN cm.user u
              WHERE cm.company.id = :companyId
            \s"""
    )
    Slice<CompanyMemberResponse> findMembershipsByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
    @Modifying
    @Query("DELETE FROM CompanyMembership cm WHERE cm.user.id= :userId AND cm.company.id= :companyId AND cm.role <> com.goat.HireHub.company.Role.ADMIN")
    int deleteByUserIdAndCompanyId(@Param("userId")Long userId,@Param("companyId")Long companyId);
    @Modifying
    @Query(
            value= """
                      UPDATE company_memberships
                      SET role= :newRole
                      WHERE user_id= :userId
                      AND company_id= :companyId
                    """,
            nativeQuery = true
    )
    int updateMembershipRole(@Param("userId")Long userId,
                             @Param("companyId")Long companyId,
                             @Param("newRole")String role
                             );
}
