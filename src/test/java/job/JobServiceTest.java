package job;


import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyMembership;
import com.goat.HireHub.company.CompanyService;
import com.goat.HireHub.company.Role;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.Job;
import com.goat.HireHub.job.JobRepository;
import com.goat.HireHub.job.JobService;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

     @Test
    void deleteById_WhenUserInCompanyButNotAdminOrRecruiter_throwsAccessDenied(){

          Company company=mock(Company.class);
          when(company.getId()).thenReturn(1L);
          Job job=mock(Job.class);
          when(job.getCompany()).thenReturn(company);
          when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

          CompanyMembership companyMembership=mock(CompanyMembership.class);
          when(companyMembership.getRole()).thenReturn(Role.EMPLOYEE);

          User user=mock(User.class);
          when(user.getId()).thenReturn(1L);
          when(securityUtils.getCurrentUser()).thenReturn(user);
          when(companyService.findOrThrow(1L)).thenReturn(company);
          when(companyService.findMemberShipOrThrow(1L,1L)).thenReturn(companyMembership);

         assertThrows(AccessDeniedException.class,()->jobService.deleteById(1L));

         verify(jobRepository,never()).deleteById(any());
         verify(jobRepository,never()).delete(job);

     }

     @Test
    void deleteById_WhenUserInCompanyAndISAdmin_DeletesSuccessfully(){

          setUpAdminOrRecruiter(Role.ADMIN);

     }


    @Test
    void deleteById_WhenUserInCompanyAndISRecruiter_DeletesSuccessfully(){

        setUpAdminOrRecruiter(Role.RECRUITER);

    }

    void setUpAdminOrRecruiter(Role role){
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

        assertDoesNotThrow(()->jobService.deleteById(1L));
        verify(jobRepository,times(1)).delete(job);
    }



}
