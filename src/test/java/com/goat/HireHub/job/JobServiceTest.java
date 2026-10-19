package com.goat.HireHub.job;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyMembership;
import com.goat.HireHub.company.CompanyService;
import com.goat.HireHub.company.Role;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.dto.JobPatchRequest;
import com.goat.HireHub.job.dto.JobRequest;
import com.goat.HireHub.job.dto.JobResponse;
import com.goat.HireHub.job.dto.JobUpdateRequest;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

     /*
     * get job by id tests
     * A job that does not exist should throw a resource not found exception
     * For a job that exists check whether the response fields matches the value that was stored
     * */

     @Test
     void getJobById_WhenJobDoesNotExist_throwsNotFound(){
           when(jobRepository.findById(99L)).thenReturn(Optional.empty());
           assertThrows(ResourceNotFoundException.class,()->jobService.getById(99L));
     }

    @Test
    void getById_HappyPath_ReturnsJobResponseWithRightFields() {
        Company company = mock(Company.class);
        when(company.getId()).thenReturn(10L);

        Job job = mock(Job.class);
        when(job.getId()).thenReturn(5L);
        when(job.getTitle()).thenReturn("QA Engineer");
        when(job.getDescription()).thenReturn("Testing frameworks");
        when(job.getSalary()).thenReturn(BigDecimal.valueOf(80000));
        when(job.getCompany()).thenReturn(company);

        when(jobRepository.findById(5L)).thenReturn(Optional.of(job));

        JobResponse response = jobService.getById(5L);

        assertNotNull(response);
        assertEquals(5L, response.id());
        assertEquals("QA Engineer", response.title());
        assertEquals(BigDecimal.valueOf(80000), response.salary());
    }

    /*
    *  Add job tests
    * A user who doesn't work for a company can't post jobs for this company
    * A user who is an employee of a company but they are not admin or recruiter cannot post jobs for this company
    * admins and recruiters can
    * */

    @Test
    void addJob_AsOutsider_ThrowsAccessDeniedAndSaveIsNeverCalled() {

        JobRequest request = new JobRequest("React Dev", "Frontend description", BigDecimal.valueOf(90000), 1L);

        Company company = mock(Company.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);

        when(companyService.findOrThrow(1L)).thenReturn(company);
        when(securityUtils.getCurrentUser()).thenReturn(user);

        when(companyService.findMemberShipOrThrow(42L, 1L))
                .thenThrow(new AccessDeniedException("not a member"));

        assertThrows(AccessDeniedException.class, () -> jobService.addJob(request));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @ParameterizedTest
    @EnumSource(value=Role.class,names={"ADMIN","RECRUITER"})
    void addJob_AsAdmin_SavesJobWithRightFieldsUsingArgumentCaptor(Role role) {

        JobRequest request = new JobRequest("Cloud Architect", "AWS specialist", BigDecimal.valueOf(150000), 1L);

        Company company = mock(Company.class);
        User user = mock(User.class);
        CompanyMembership membership = mock(CompanyMembership.class);

        when(companyService.findOrThrow(1L)).thenReturn(company);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(42L);
        when(companyService.findMemberShipOrThrow(42L, 1L)).thenReturn(membership);
        when(membership.getRole()).thenReturn(role);

        Job savedMock = new Job();
        savedMock.setCompany(company);
        when(jobRepository.save(any(Job.class))).thenReturn(savedMock);

        jobService.addJob(request);

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository, times(1)).save(jobCaptor.capture());

        Job capturedJob = jobCaptor.getValue();

        assertEquals("Cloud Architect", capturedJob.getTitle());
        assertEquals("AWS specialist", capturedJob.getDescription());
        assertEquals(BigDecimal.valueOf(150000), capturedJob.getSalary());
        assertEquals(company, capturedJob.getCompany());
    }

    @Test
    void addJob_AsNormalEmployee_ThrowsAccessDeniedAndNeverSaves() {
        JobRequest request = new JobRequest("Data Analyst", "Description", BigDecimal.valueOf(70000), 1L);
        Company company = mock(Company.class);
        User user = mock(User.class);
        CompanyMembership membership = mock(CompanyMembership.class);

        when(companyService.findOrThrow(1L)).thenReturn(company);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(42L);
        when(companyService.findMemberShipOrThrow(42L, 1L)).thenReturn(membership);
        when(membership.getRole()).thenReturn(Role.EMPLOYEE);

        assertThrows(AccessDeniedException.class, () -> jobService.addJob(request));
        verify(jobRepository, never()).save(any(Job.class));
    }

    /*
    * patch and Update job tests
    * A member of company A cannot update a company that belongs to company B regardless of their role in company A
    * A member of a company cannot update a job of that company if they are not authorized to do so
    * */

    @Test
    void updateJob_ForJobInAnotherCompany_ThrowsAccessDenied() {

        JobUpdateRequest updateRequest = new JobUpdateRequest("New Title", "New Description", BigDecimal.valueOf(60000));

        Company realOwningCompany = mock(Company.class);
        when(realOwningCompany.getId()).thenReturn(99L);

        Job savedJob = mock(Job.class);
        when(savedJob.getCompany()).thenReturn(realOwningCompany);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(savedJob));

        User standardUser = mock(User.class);
        when(standardUser.getId()).thenReturn(100L);
        when(securityUtils.getCurrentUser()).thenReturn(standardUser);

        when(companyService.findOrThrow(99L)).thenReturn(realOwningCompany);

        when(companyService.findMemberShipOrThrow(100L, 99L))
                .thenThrow(new AccessDeniedException("User has no connection to this organization"));


        assertThrows(AccessDeniedException.class, () -> jobService.updateJob(1L, updateRequest));

        verify(savedJob, never()).setTitle(anyString());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMIN", "RECRUITER"})
    void updateJob_AsAuthorizedRole_UpdatesFieldsSuccessfully(Role authorizedRole) {

        JobUpdateRequest updateRequest = new JobUpdateRequest("Staff Engineer", "New Specs", BigDecimal.valueOf(130000));
        Company company = mock(Company.class);
        when(company.getId()).thenReturn(1L);

        Job savedJob = new Job();
        savedJob.setCompany(company);

        User user = mock(User.class);
        CompanyMembership membership = mock(CompanyMembership.class);

        when(jobRepository.findById(5L)).thenReturn(Optional.of(savedJob));
        when(companyService.findOrThrow(1L)).thenReturn(company);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(42L);
        when(companyService.findMemberShipOrThrow(42L, 1L)).thenReturn(membership);
        when(membership.getRole()).thenReturn(authorizedRole);
        JobResponse response = jobService.updateJob(5L, updateRequest);
        assertEquals("Staff Engineer", savedJob.getTitle());
        assertEquals("New Specs", savedJob.getDescription());
        assertEquals(BigDecimal.valueOf(130000), savedJob.getSalary());
    }

    @Test
    void updateJob_AsNormalEmployeeInsideCompany_ThrowsAccessDenied() {

        JobUpdateRequest updateRequest = new JobUpdateRequest("New Title", "New Description", BigDecimal.valueOf(60000));
        Company company = mock(Company.class);
        when(company.getId()).thenReturn(1L);

        Job savedJob = mock(Job.class);
        when(savedJob.getCompany()).thenReturn(company);
        when(jobRepository.findById(5L)).thenReturn(Optional.of(savedJob));

        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(securityUtils.getCurrentUser()).thenReturn(user);

        CompanyMembership companyMembership = mock(CompanyMembership.class);
        when(companyMembership.getRole()).thenReturn(Role.EMPLOYEE);

        when(companyService.findOrThrow(1L)).thenReturn(company);
        when(companyService.findMemberShipOrThrow(42L, 1L)).thenReturn(companyMembership);

        assertThrows(AccessDeniedException.class, () -> jobService.updateJob(5L, updateRequest));
        verify(savedJob, never()).setTitle(anyString());
    }


    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMIN", "RECRUITER"})
    void patchJob_AsAuthorizedRole_PatchesPartialFieldsSuccessfully(Role authorizedRole) {

        JobPatchRequest patchRequest = new JobPatchRequest(null, "Patched Description", BigDecimal.valueOf(110000));
        Company company = mock(Company.class);
        when(company.getId()).thenReturn(1L);

        Job savedJob = new Job();
        savedJob.setTitle("Preserved Original Title");
        savedJob.setCompany(company);

        User user = mock(User.class);
        CompanyMembership membership = mock(CompanyMembership.class);

        when(jobRepository.findById(5L)).thenReturn(Optional.of(savedJob));
        when(companyService.findOrThrow(1L)).thenReturn(company);
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(42L);
        when(companyService.findMemberShipOrThrow(42L, 1L)).thenReturn(membership);
        when(membership.getRole()).thenReturn(authorizedRole);

        jobService.patchJob(5L, patchRequest);


        assertEquals("Preserved Original Title", savedJob.getTitle());
        assertEquals("Patched Description", savedJob.getDescription());
    }


    @Test
    void patchJob_AsNormalEmployeeInsideCompany_ThrowsAccessDenied() {

        JobPatchRequest patchRequest = new JobPatchRequest("New Title", null, null);
        Company company = mock(Company.class);
        when(company.getId()).thenReturn(1L);

        Job savedJob = mock(Job.class);
        when(savedJob.getCompany()).thenReturn(company);
        when(jobRepository.findById(5L)).thenReturn(Optional.of(savedJob));

        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(securityUtils.getCurrentUser()).thenReturn(user);

        CompanyMembership companyMembership = mock(CompanyMembership.class);
        when(companyMembership.getRole()).thenReturn(Role.EMPLOYEE);

        when(companyService.findOrThrow(1L)).thenReturn(company);
        when(companyService.findMemberShipOrThrow(42L, 1L)).thenReturn(companyMembership);

        assertThrows(AccessDeniedException.class, () -> jobService.patchJob(5L, patchRequest));
        verify(savedJob, never()).setTitle(anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "UPDATE",
            "PATCH"
    })
    void modifyJob_AsCompleteOutsider_ThrowsAccessDenied(String actionType) {

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(1L);

        Job savedJob = mock(Job.class);
        when(savedJob.getCompany()).thenReturn(company);
        when(jobRepository.findById(5L)).thenReturn(Optional.of(savedJob));

        User user = mock(User.class);
        when(user.getId()).thenReturn(999L);
        when(securityUtils.getCurrentUser()).thenReturn(user);

        when(companyService.findOrThrow(1L)).thenReturn(company);

        when(companyService.findMemberShipOrThrow(999L, 1L))
                .thenThrow(new AccessDeniedException("User has no records tied to this company"));


        if ("UPDATE".equals(actionType)) {
            JobUpdateRequest req = new JobUpdateRequest("Title", "Desc", BigDecimal.valueOf(50000));
            assertThrows(AccessDeniedException.class, () -> jobService.updateJob(5L, req));
        } else {
            JobPatchRequest req = new JobPatchRequest("Title", null, null);
            assertThrows(AccessDeniedException.class, () -> jobService.patchJob(5L, req));
        }

        verify(savedJob, never()).setTitle(anyString());
    }

    /*
    *  Delete job tests
    * */


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
