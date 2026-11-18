package com.goat.HireHub.company.membership;

import com.goat.HireHub.company.Role;
import com.goat.HireHub.company.membership.dto.CompanyMemberResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership,Long> {
    Optional<CompanyMembership> findByUserIdAndCompanyId(Long userId,Long companyId);
    @Query(
          "SELECT cm.role from CompanyMembership cm WHERE cm.user.id= :userId AND cm.company.id= :companyId"
    )
    Optional<Role> findRoleByUserIdAndCompanyId(@Param("userId")Long userId,@Param("companyId")Long companyId);
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
}
