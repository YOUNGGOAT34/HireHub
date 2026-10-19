package com.goat.HireHub.job;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyMembership;
import com.goat.HireHub.company.CompanyService;
import com.goat.HireHub.company.Role;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {
     @Mock
    JobRepository jobRepository;
     @Mock
    CompanyService companyService;
     @Mock
    SecurityUtils securityUtils;

     @InjectMocks
    JobService jobService;

     @Test
    void getById_WhenJobDoesNotExist_throwsNotFound(){
          //set up
         when(jobRepository.findById(99L)).thenReturn(Optional.empty());

         assertThrows(ResourceNotFoundException.class,()->jobService.getById(99L));
     }

     @Test

     void deleteById_WhenUserNotInCompany_throwsAccessDenied(){
          Company company=mock(Company.class);
          when(company.getId()).thenReturn(1L);

          Job job=mock(Job.class);
         when(job.getCompany()).thenReturn(company);
          when(jobRepository.findById(5L)).thenReturn(Optional.of(job));


          User user=mock(User.class);
          when(user.getId()).thenReturn(42L);
          when(securityUtils.getCurrentUser()).thenReturn(user);
          when(companyService.findOrThrow(1L)).thenReturn(company);
          when(companyService.findMemberShipOrThrow(42L,1L)).
                  thenThrow(new AccessDeniedException("not a member"));
          assertThrows(AccessDeniedException.class,()->jobService.deleteById(5L));

          verify(jobRepository,never()).deleteById(any());
          verify(jobRepository,never()).delete(job);

     }

     @ParameterizedTest
     @EnumSource(value=Role.class,names={"ADMIN","RECRUITER"},mode=EnumSource.Mode.EXCLUDE)
    void deleteById_WhenUserInCompanyButNotAdminOrRecruiter_throwsAccessDenied(Role role){
          Job job=givenJobInCompanyWhereUserHasRole(role);
         assertThrows(AccessDeniedException.class,()->jobService.deleteById(1L));
         verify(jobRepository,never()).deleteById(any());
         verify(jobRepository,never()).delete(job);

     }



    @ParameterizedTest
    @EnumSource(value=Role.class,names={"ADMIN","RECRUITER"})
    void deleteById_WhenUserInCompanyAndISRecruiterOrAdmin_DeletesSuccessfully(Role role){
        Job job=givenJobInCompanyWhereUserHasRole(role);
        assertDoesNotThrow(()->jobService.deleteById(1L));
        verify(jobRepository,times(1)).delete(job);

    }

    Job givenJobInCompanyWhereUserHasRole(Role role){
        Company company=mock(Company.class);
        when(company.getId()).thenReturn(1L);
        Job job=mock(Job.class);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(job.getCompany()).thenReturn(company);
        when(companyService.findOrThrow(1L)).thenReturn(company);

        User user=mock(User.class);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);

        CompanyMembership companyMembership=mock(CompanyMembership.class);
        when(companyMembership.getRole()).thenReturn(role);

        when(companyService.findMemberShipOrThrow(1L,1L)).thenReturn(companyMembership);

        return job;
    }



}
