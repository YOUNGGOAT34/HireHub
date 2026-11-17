package com.goat.HireHub.company.membership;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership,Long> {
    Optional<CompanyMembership> findByUserIdAndCompanyId(Long userId,Long companyId);
    @Query(value =
            """
             SELECT new com.goat.HireHub.company.membership.dto.CompanyMemberResponse(
                u.id,
                u.firstName,
                u.lastName,
                u.role
             )
             FROM CompanyMembership cm 
             JOIN cm.user u
              WHERE cm.company.id = :companyId
             """
    )
    Slice<CompanyMembership> findMembershipsByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
