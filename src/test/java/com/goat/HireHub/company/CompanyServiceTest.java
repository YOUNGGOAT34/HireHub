package com.goat.HireHub.company;

import com.goat.HireHub.company.dto.CompanyPatchRequest;
import com.goat.HireHub.company.dto.CompanyRequest;
import com.goat.HireHub.company.dto.CompanyResponse;
import com.goat.HireHub.company.membership.CompanyMembership;
import com.goat.HireHub.company.membership.CompanyMembershipRepository;
import com.goat.HireHub.company.membership.MemberAddedOrDeletedEvent;
import com.goat.HireHub.company.membership.dto.CompanyMemberResponse;
import com.goat.HireHub.exception.BadRequestException;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.AuthenticatedUser;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import com.goat.HireHub.user.dto.UserSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {
    private final Long USER_ID=42L;
    private final Long COMPANY_ID=1L;
    @Mock
    private CompanyRepository companyRepository;
    @Mock private CompanyMembershipRepository companyMembershipRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher publisher;

    @InjectMocks
    private CompanyService companyService;

    MockMvc mockMvc;

    //get and find by id tests
    @Test
    void getCompanyById_HappyPath_ReturnsExpectedResponseFields() {
        Company company = new Company();
        company.setId(1L);
        company.setName("Goat Industries");
        company.setDescription("Top Tier Software");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        CompanyResponse response = companyService.getCompanyById(1L);
        assertNotNull(response);
        assertEquals("Goat Industries", response.name());
        assertEquals("Top Tier Software", response.description());
    }

    @Test
    void getCompanyById_WhenCompanyDoesNotExist_ThrowsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> companyService.getCompanyById(99L));
    }

    //add company
    @Test
    void addCompany_HappyPath_SavesCompanyAndCreatesAdminMembership() {
        CompanyRequest request = new CompanyRequest("New Corp", "Innovation focused");
        givenCurrentUser();
        User userProxy=mock(User.class);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(userProxy);
        Company savedCompanyMock = new Company();
        savedCompanyMock.setId(10L);
        savedCompanyMock.setName("New Corp");
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompanyMock);

        CompanyResponse response=companyService.addCompany(request);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        assertEquals("New Corp", companyCaptor.getValue().getName());
        assertEquals("Innovation focused", companyCaptor.getValue().getDescription());

        ArgumentCaptor<CompanyMembership> membershipCaptor = ArgumentCaptor.forClass(CompanyMembership.class);
        verify(companyMembershipRepository).save(membershipCaptor.capture());
        CompanyMembership generatedMembership = membershipCaptor.getValue();
        assertEquals(userProxy, generatedMembership.getUser());
        assertEquals(savedCompanyMock, generatedMembership.getCompany());
        assertEquals(Role.ADMIN, generatedMembership.getRole());
    }

    //mutation security tests,delete,update,patch
    @Test
    void modifyCompany_WhenCompanyDoesNotExist_ThrowsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> companyService.deleteById(99L));
        verify(companyRepository, never()).deleteById(anyLong());
        verifyNoInteractions(publisher);
    }

    @Test
    void deleteById_AsAdmin_DeletesSuccessfully() {
        givenUserHasRole("ADMIN");
        assertDoesNotThrow(() -> companyService.deleteById(1L));
        verify(companyRepository).deleteById(1L);
        verify(publisher).publishEvent(any(MemberAddedOrDeletedEvent.class));
    }

    @ParameterizedTest
    @ValueSource(strings={"EMPLOYEE","RECRUITER","NONE"})
    void deleteById_AsNonAdminRole_ThrowsAccessDenied(String nonAdminRole) {
        givenUserHasRole(nonAdminRole);
        assertThrows(AccessDeniedException.class, () -> companyService.deleteById(1L));
        verify(companyRepository, never()).deleteById(anyLong());
        verifyNoInteractions(publisher);
    }

    @Test
    void updateCompany_AsAdmin_UpdatesFieldsSuccessfully() throws Exception{
        givenUserHasRole("ADMIN");
        companyService.updateCompany(COMPANY_ID,new CompanyRequest("Updated Name","Updated description"));
        verify(companyRepository).updateCompany(COMPANY_ID,"UpdatedName","Updated description");
    }

    @ParameterizedTest
    @ValueSource(strings={"EMPLOYEE","RECRUITER","NONE"})
    void updateCompany_AsNonAdmin_ThrowsAccessDenied(String role){
         givenUserHasRole(role);
         assertThrows(AccessDeniedException.class,()->companyService.updateCompany(COMPANY_ID,new CompanyRequest("X","Y")));
         verify(companyRepository,never()).updateCompany(anyLong(),anyString(),anyString());
    }

    @Test
    void patchCompany_AsAdmin_UpdatesFieldsSuccessfully(){
        givenUserHasRole("ADMIN");
        companyService.patchCompany(COMPANY_ID,new CompanyPatchRequest("New Name",null));
        verify(companyRepository).patchCompany(COMPANY_ID,"New Name",null);
    }

    @Test
    void patchCompany_WithEmptyBody_ThrowsBadRequestBeforeAuthorizing(){
          assertThrows(BadRequestException.class,()->companyService.patchCompany(COMPANY_ID,new CompanyPatchRequest(null,null)));
          verifyNoInteractions(companyMembershipRepository,companyRepository,securityUtils);
    }
    @ParameterizedTest
    @ValueSource(strings={"EMPLOYEE","RECRUITER","NONE"})
    void patchCompany_AsNonAdminRole_ThrowsAccessDenied(String role) throws Exception{
        givenUserHasRole(role);
        assertThrows(AccessDeniedException.class, () -> companyService.patchCompany(COMPANY_ID, new CompanyPatchRequest("New Name",null)));
        verify(companyRepository,never()).patchCompany(anyLong(),anyString(),anyString());
    }

    //add member workflow tests
    @Test
    void addMember_AsAdminAndNewUser_SavesMembershipSuccessfully() {
        givenUserHasRole("ADMIN");
        UserSummary targetUser=mock(UserSummary.class);
        when(targetUser.getId()).thenReturn(200L);
        when(targetUser.getFirstName()).thenReturn("goatF");
        when(targetUser.getLastName()).thenReturn("goatL");
        when(userRepository.findByEmailIgnoreCase("invitee@hub.com")).thenReturn(Optional.of(targetUser));

        User userProxy=mock(User.class);
        Company companyProxy=mock(Company.class);
        when(userRepository.getReferenceById(200L)).thenReturn(userProxy);
        when(companyRepository.getReferenceById(COMPANY_ID)).thenReturn(companyProxy);

        CompanyMemberResponse response=companyService.addMember(COMPANY_ID,"invitee@hub.com",Role.RECRUITER);

        ArgumentCaptor<CompanyMembership> membershipCaptor = ArgumentCaptor.forClass(CompanyMembership.class);
        verify(companyMembershipRepository).saveAndFlush(membershipCaptor.capture());
        CompanyMembership savedMembership = membershipCaptor.getValue();
        assertEquals(userProxy, savedMembership.getUser());
        assertEquals(companyProxy, savedMembership.getCompany());
        assertEquals(Role.RECRUITER, savedMembership.getRole());
        verify(publisher).publishEvent(any(MemberAddedOrDeletedEvent.class));
        assertNotNull(response);
    }

    @Test
    void addMember_WhenUserEmailUnknown_ThrowsResourceNotFound(){
        givenUserHasRole("ADMIN");
        when(userRepository.findByEmailIgnoreCase("ghost@hub.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()->companyService.addMember(COMPANY_ID,"ghost@hub.com",Role.RECRUITER));
        verify(companyMembershipRepository,never()).saveAndFlush(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void addMember_WhenTargetUserAlreadyMember_ThrowsConflictException() {
        givenUserHasRole("ADMIN");
        UserSummary targetUser = mock(UserSummary.class);
        when(targetUser.getId()).thenReturn(200L);
        when(userRepository.findByEmailIgnoreCase("duplicate@hub.com")).thenReturn(Optional.of(targetUser));
        when(userRepository.getReferenceById(200L)).thenReturn(mock(User.class));
        when(companyRepository.getReferenceById(COMPANY_ID)).thenReturn(mock(Company.class));
        when(companyMembershipRepository.saveAndFlush(any(CompanyMembership.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        ConflictException ex=assertThrows(ConflictException.class,
                ()->companyService.addMember(COMPANY_ID,"duplicate@hub.com",Role.RECRUITER)
                );
        assertTrue(ex.getMessage().contains("duplicate@hub,com"));

       verifyNoInteractions(publisher);
    }
// Arrange: Set up a company where the logged-in user is NOT an admin (e.g., RECRUITER, EMPLOYEE)

    @ParameterizedTest
    @ValueSource(strings={"RECRUITER","EMPLOYEE","NONE"})
    void addMember_AsNonAdminRole_ThrowsAccessDenied(String nonAdminRole) {
        givenUserHasRole(nonAdminRole);
        assertThrows(AccessDeniedException.class, () ->
                companyService.addMember(1L, "newuser@hub.com", Role.EMPLOYEE)
        );

        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
        verify(companyMembershipRepository, never()).save(any(CompanyMembership.class));
        verifyNoInteractions(publisher);
    }

    //logged in user
    private void givenCurrentUser(){
         AuthenticatedUser currentUser=mock(AuthenticatedUser.class);
         when(currentUser.id()).thenReturn(USER_ID);
         when(securityUtils.getCurrentUser()).thenReturn(currentUser);
    }

    //when logged-in user has a role in a company
    private void givenUserHasRole(String role){
         givenCurrentUser();
         when(companyMembershipRepository.findRoleByUserIdAndCompanyId(USER_ID,COMPANY_ID))
                 .thenReturn(Optional.of(role));
    }

    //logged in member has no membership
    private void givenUserHasNoMembership(){
        givenCurrentUser();
        when(companyMembershipRepository.findRoleByUserIdAndCompanyId(USER_ID,COMPANY_ID))
                .thenReturn(Optional.empty());
    }

}
