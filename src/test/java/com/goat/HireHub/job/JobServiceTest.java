package com.goat.HireHub.job;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.membership.CompanyMembership;
import com.goat.HireHub.company.CompanyService;
import com.goat.HireHub.company.Role;
import com.goat.HireHub.exception.BadRequestException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.dto.JobPatchRequest;
import com.goat.HireHub.job.dto.JobRequest;
import com.goat.HireHub.job.dto.JobResponse;
import com.goat.HireHub.job.dto.JobUpdateRequest;
import com.goat.HireHub.job.requirement.JobRequirementsRepository;
import com.goat.HireHub.security.AuthenticatedUser;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.skill.SkillRepository;
import com.goat.HireHub.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    private static final Long USER_ID=42L;
    private static final Long COMPANY_ID=1L;
    private static final Long JOB_ID=5L;

     @Mock
    JobRepository jobRepository;
     @Mock
    CompanyService companyService;
     @Mock
    SecurityUtils securityUtils;
     @Mock
     private JobRequirementsRepository requirementsRepository;

     @Mock private SkillRepository skillRepository;

     @InjectMocks
    JobService jobService;

     /*
     * get job by id tests
     * A job that does not exist should throw a resource not found exception
     * For a job that exists check whether the response fields matches the value that was stored
     * */

     @Test
     void getJobById_WhenJobDoesNotExist_throwsNotFound(){
           when(jobRepository.findWithCompanyById(99L)).thenReturn(Optional.empty());
           assertThrows(ResourceNotFoundException.class,()->jobService.getById(99L));
     }

    @Test
    void getById_HappyPath_ReturnsJobResponseWithRightFields() {
         Company company=new Company();
         company.setId(10L);
         company.setName("Safaricom");

         Job job=new Job();
         job.setTitle("QA Engineer");
         job.setDescription("Testing frameworks");
         job.setSalary(BigDecimal.valueOf(80000));
         job.setCompany(company);

         when(jobRepository.findWithCompanyById(JOB_ID)).thenReturn(Optional.of(job));
         JobResponse response=jobService.getById(JOB_ID);

         assertNotNull(response);
         assertEquals("QA Engineer",response.title());
         assertEquals(0,BigDecimal.valueOf(80000).compareTo(response.salary()));
    }

    /*
    *  Add job tests
    * A user who doesn't work for a company can't post jobs for this company
    * A user who is an employee of a company, but they are not admin or recruiter cannot post jobs for this company
    * admins and recruiters can
    * */

    @Test
    void addJob_WhenCompanyMissing_ThrowsNotFoundBeforeAuthorizing(){
        JobRequest request=mock(JobRequest.class);
        when(request.companyId()).thenReturn(COMPANY_ID);
        when(companyService.findOrThrow(COMPANY_ID))
                .thenThrow(new ResourceNotFoundException("company",COMPANY_ID));

        assertThrows(ResourceNotFoundException.class,()->jobService.addJob(request));

         verifyNoInteractions(securityUtils);
         verify(jobRepository,never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings={"EMPLOYEE","NONE"})
    void addJob_AsNonRecruiterOrAdmin_ThrowsAccessDenied(String role){
        JobRequest request=mock(JobRequest.class);
        when(request.companyId()).thenReturn(COMPANY_ID);
        when(companyService.findOrThrow(COMPANY_ID)).thenReturn(new Company());
        givenUserHasRole(COMPANY_ID,role);
        assertThrows(AccessDeniedException.class,()->jobService.addJob(request));
        verify(jobRepository,never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings={"ADMIN","RECRUITER"})
    void addJob_AsAdminOrRecruiter_SavesJobWithRightFieldsUsingArgumentCaptor(String role) {

         Company company=new Company();
         company.setId(COMPANY_ID);
         company.setName("Safaricom");
         JobRequest request=mock(JobRequest.class);
         when(request.companyId()).thenReturn(COMPANY_ID);
         when(request.title()).thenReturn("Cloud Architect");
         when(request.description()).thenReturn("AWS specialist");
         when(request.salary()).thenReturn(BigDecimal.valueOf(150000));
         when(request.location()).thenReturn("Nairobi");
         when(request.workplaceType()).thenReturn(WorkplaceType.ON_SITE);
         when(request.employmentType()).thenReturn(EmploymentType.FULL_TIME);
         when(request.experienceLevel()).thenReturn(ExperienceLevel.ENTRY_LEVEL);
         when(companyService.findOrThrow(COMPANY_ID))
                 .thenReturn(company);
         givenUserHasRole(COMPANY_ID,role);
         when(jobRepository.save(any(Job.class))).thenAnswer(inv->inv.getArgument(0));

         jobService.addJob(request);
         ArgumentCaptor<Job> captor=ArgumentCaptor.forClass(Job.class);
         verify(jobRepository).save(captor.capture());
         Job savedJob=captor.getValue();

         assertEquals("Cloud Architect",savedJob.getTitle());
         assertEquals("AWS specialist",savedJob.getDescription());
         assertEquals(BigDecimal.valueOf(150000),savedJob.getSalary());
         assertSame(company,savedJob.getCompany());
    }

    /*
     *  Delete job tests
     * you cannot delete a job if you are not a member of the company that posted that job
     * you cannot delete a job if you are a member of the company that posted job if you have no authorization
     * */
    @Test
    void deleteById_WhenJobDoesNotExist_ThrowsNotFoundExceptionAndTouchesNothingElse(){
         when(jobRepository.findCompanyIdByJobId(99L)).thenReturn(Optional.empty());
         assertThrows(ResourceNotFoundException.class,()->jobService.deleteById(99L));
         verifyNoInteractions(securityUtils,companyService,requirementsRepository);
         verify(jobRepository,never()).deleteJobById(anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings={"EMPLOYEE","NONE"})
    void deleteById_AsNonAdminOrRecruiter_ThrowsAccessDenied(String role){
         givenJobInCompany(JOB_ID,COMPANY_ID);
         givenUserHasRole(COMPANY_ID,role);
         assertThrows(AccessDeniedException.class,()->jobService.deleteById(JOB_ID));
         verifyNoInteractions(requirementsRepository);
         verify(jobRepository,never()).deleteById(anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings={"ADMIN","RECRUITER"})
    void deleteById_AsAdminOrRecruiter_DeletesRequirementsThenJob(String role){
         givenJobInCompany(JOB_ID,COMPANY_ID);
         givenUserHasRole(COMPANY_ID,role);
         assertDoesNotThrow(()->jobService.deleteById(JOB_ID));
         var order=inOrder(requirementsRepository,jobRepository);
         order.verify(requirementsRepository).deleteAllByJobId(JOB_ID);
         order.verify(jobRepository).deleteJobById(JOB_ID);
    }


    /*
    * patch and Update job tests
    * A member of company A cannot update a company that belongs to company B regardless of their role in company A
    *  member of a company cannot update a job of that company if they are not authorized to do so
    * */

    @Test
    void updateJob_WhenJobDoesNotExist_ThrowsNotFound(){
        JobUpdateRequest request=mock(JobUpdateRequest.class);
        when(jobRepository.findCompanyIdByJobId(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()->jobService.updateJob(99L,request));
        verifyNoInteractions(securityUtils);
    }

    @ParameterizedTest
    @ValueSource(strings={"EMPLOYEE","NONE"})
    void updateJob_AsNonAdminOrRecruiter_ThrowsAccessDenied(String role){
        JobUpdateRequest request=mock(JobUpdateRequest.class);
        givenJobInCompany(JOB_ID,COMPANY_ID);
        givenUserHasRole(COMPANY_ID,role);

        assertThrows(AccessDeniedException.class,()->jobService.updateJob(JOB_ID,request));
        verify(jobRepository,never()).updateJob(anyLong(),any(BigDecimal.class),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString());

    }

    void updateJob_AsAdminOrRecruiter_PassesAllValuesToRepository(String role){
         JobUpdateRequest request=mock(JobUpdateRequest.class);
         when(request.salary()).thenReturn(BigDecimal.valueOf(130000));
         when(request.title()).thenReturn("Staff Engineer");
         when(request.description()).thenReturn("New Specs");
         when(request.url()).thenReturn("https://example.com");
         when(request.location()).thenReturn("Nairobi");
         when(request.workplaceType()).thenReturn(WorkplaceType.ON_SITE);
         when(request.employmentType()).thenReturn(EmploymentType.FULL_TIME);
         when(request.experienceLevel()).thenReturn(ExperienceLevel.ENTRY_LEVEL);

         givenJobInCompany(JOB_ID,COMPANY_ID);
         givenUserHasRole(COMPANY_ID,role);

         jobService.updateJob(JOB_ID,request);

         verify(jobRepository).updateJob(JOB_ID,BigDecimal.valueOf(130000),
                 "Staff Engineer","New Specs","https://example.com","Nariobi",
                 WorkplaceType.ON_SITE.name(),EmploymentType.FULL_TIME.name(),ExperienceLevel.ENTRY_LEVEL.name()
                 );

    }

    @Test
    void patchJob_WithEmptyBody_ThrowsBadRequestBeforeAnyLookup(){
          JobPatchRequest request=mock(JobPatchRequest.class);
          assertThrows(BadRequestException.class,()->jobService.patchJob(JOB_ID,request));
          verifyNoInteractions(jobRepository,securityUtils,companyService);
    }

    @Test
    void patchJob_WhenJobDoesNotExist_ThrowsNotFound(){
        JobPatchRequest request=mock(JobPatchRequest.class);
        when(request.title()).thenReturn("New Title");
        when(jobRepository.findCompanyIdByJobId(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()->jobService.patchJob(99L,request));
        verifyNoInteractions(securityUtils);
    }

    @ParameterizedTest
    @ValueSource(strings={"EMPLOYEE","NONE"})
    void patchJob_AsNonAdminOrRecruiter_ThrowsAccessDenied(String role){
         JobPatchRequest request=mock(JobPatchRequest.class);
         when(request.title()).thenReturn("Hacked");
         givenUserHasRole(COMPANY_ID,role);
         givenJobInCompany(JOB_ID,COMPANY_ID);

         assertThrows(AccessDeniedException.class,()->jobService.patchJob(JOB_ID,request));
         verify(jobRepository,never()).patchJob(anyLong(),any(BigDecimal.class),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString());
    }

    @ParameterizedTest
    @ValueSource(strings={"ADMIN","RECRUITER"})
    void patchJob_AsAdminOrRecruiter_PassesNullsForUntouchedFields(String role){
         JobPatchRequest request=mock(JobPatchRequest.class);
         when(request.description()).thenReturn("Patched Description");
         when(request.salary()).thenReturn(BigDecimal.valueOf(110000));
         givenJobInCompany(JOB_ID,COMPANY_ID);
         givenUserHasRole(COMPANY_ID,role);
         jobService.patchJob(JOB_ID,request);

         verify(jobRepository).patchJob(JOB_ID,BigDecimal.valueOf(110000),null,"Patched Description",null,
                 null,null,null,null
         );
    }


    @Test
    void saveJobSkills_TrimsDedupesIgnoringCaseAndDropsBlackNullAndTooLong(){
         Instant seen=Instant.now();
         List<String> raw= Arrays.asList("Java","java",null,"","Spring","x".repeat(101));
         jobService.saveJobSkills(7L,raw,seen);
         verify(skillRepository).insertMissingSkills(new String[]{"Java","Spring"});
         verify(jobRepository).linkSkillsToJob(7L,new String[]{"java","spring"});
         verify(jobRepository).unlinkSkillsFromJob(7L,new String[]{"java","spring"});
         verify(requirementsRepository).clearStale(7L,seen);

    }

    @Test
    void saveJobSkills_WhenNothingValid_SkipsInsertAndLinkButStillUnlinksAndClearsStale(){
         Instant seen=Instant.now();
         jobService.saveJobSkills(7L,List.of(" ",""),seen);
         verifyNoInteractions(skillRepository);
         verify(jobRepository,never()).linkSkillsToJob(anyLong(),any());
         verify(jobRepository).unlinkSkillsFromJob(eq(7L),any(String[].class));
         verify(requirementsRepository).clearStale(7L,seen);
    }

   //helpers
    // logged-in user(id 42) has this role in the given company
    private  void givenUserHasRole(Long companyId,String role){
         AuthenticatedUser user=mock(AuthenticatedUser.class);
         when(user.id()).thenReturn(USER_ID);
         when(securityUtils.getCurrentUser()).thenReturn(user);
         when(companyService.findRole(USER_ID,companyId)).thenReturn(role);
    }

    //the job exists and belongs to a given company

    private void givenJobInCompany(Long jobId,Long companyId){
         when(jobRepository.findCompanyIdByJobId(jobId)).thenReturn(Optional.of(companyId));
    }

}
