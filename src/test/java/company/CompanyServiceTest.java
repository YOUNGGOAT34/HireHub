package company;

import com.goat.HireHub.company.*;
import com.goat.HireHub.company.dto.CompanyPatchRequest;
import com.goat.HireHub.company.dto.CompanyRequest;
import com.goat.HireHub.company.dto.CompanyResponse;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
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

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {
    @Mock
    private CompanyRepository companyRepository;
    @Mock private CompanyMembershipRepository companyMembershipRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CompanyService companyService;

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
        User currentUser = mock(User.class);

        when(securityUtils.getCurrentUser()).thenReturn(currentUser);

        Company savedCompanyMock = new Company();
        savedCompanyMock.setId(10L);
        savedCompanyMock.setName("New Corp");
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompanyMock);

        companyService.addCompany(request);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository, times(1)).save(companyCaptor.capture());
        assertEquals("New Corp", companyCaptor.getValue().getName());
        assertEquals("Innovation focused", companyCaptor.getValue().getDescription());

        ArgumentCaptor<CompanyMembership> membershipCaptor = ArgumentCaptor.forClass(CompanyMembership.class);
        verify(companyMembershipRepository, times(1)).save(membershipCaptor.capture());

        CompanyMembership generatedMembership = membershipCaptor.getValue();
        assertEquals(currentUser, generatedMembership.getUser());
        assertEquals(savedCompanyMock, generatedMembership.getCompany());
        assertEquals(Role.ADMIN, generatedMembership.getRole());
    }

    //mutation security tests,delete,update,patch
    @Test
    void modifyCompany_WhenCompanyDoesNotExist_ThrowsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> companyService.deleteById(99L));
        verify(companyRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteById_AsAdmin_DeletesSuccessfully() {
        Company company = givenCompanyWithUserRole(Role.ADMIN);

        assertDoesNotThrow(() -> companyService.deleteById(1L));

        verify(companyRepository, times(1)).deleteById(1L);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMIN"}, mode = EnumSource.Mode.EXCLUDE)
    void deleteById_AsNonAdminRole_ThrowsAccessDenied(Role nonAdminRole) {
        givenCompanyWithUserRole(nonAdminRole);

        assertThrows(AccessDeniedException.class, () -> companyService.deleteById(1L));
        verify(companyRepository, never()).deleteById(anyLong());
    }


    @Test
    void updateCompany_AsAdmin_UpdatesFieldsSuccessfully() {
        Company company = givenCompanyWithUserRole(Role.ADMIN);
        CompanyRequest updateRequest = new CompanyRequest("Updated Name", "Updated Desc");

        CompanyResponse response = companyService.updateCompany(1L, updateRequest);

        assertEquals("Updated Name", company.getName());
        assertEquals("Updated Desc", company.getDescription());
    }

    @ParameterizedTest
    @CsvSource({
            "EMPLOYEE",
            "RECRUITER"
    })
    void patchCompany_AsNonAdminRole_ThrowsAccessDenied(String roleName) {
        Role nonAdminRole = Role.valueOf(roleName);
        Company company = givenCompanyWithUserRole(nonAdminRole);
        CompanyPatchRequest patchRequest = new CompanyPatchRequest("New Name", null);

        assertThrows(AccessDeniedException.class, () -> companyService.patchCompany(1L, patchRequest));
        assertNotEquals("New Name", company.getName()); // Confirms state preservation
    }


    //add member workflow tests
    @Test
    void addMember_AsAdminAndNewUser_SavesMembershipSuccessfully() {
        Company company = givenCompanyWithUserRole(Role.ADMIN);
        User targetUser = mock(User.class);
        when(targetUser.getId()).thenReturn(200L);

        when(userRepository.findByEmailIgnoreCase("invitee@hub.com")).thenReturn(Optional.of(targetUser));
        when(companyMembershipRepository.findByUserIdAndCompanyId(200L, 1L)).thenReturn(Optional.empty());

        when(companyMembershipRepository.save(any(CompanyMembership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        companyService.addMember(1L, "invitee@hub.com", Role.RECRUITER);

        ArgumentCaptor<CompanyMembership> membershipCaptor = ArgumentCaptor.forClass(CompanyMembership.class);
        verify(companyMembershipRepository, times(1)).save(membershipCaptor.capture());

        CompanyMembership savedMembership = membershipCaptor.getValue();
        assertEquals(targetUser, savedMembership.getUser());
        assertEquals(company, savedMembership.getCompany());
        assertEquals(Role.RECRUITER, savedMembership.getRole());
    }

    @Test
    void addMember_WhenTargetUserAlreadyMember_ThrowsConflictException() {
        givenCompanyWithUserRole(Role.ADMIN);
        User targetUser = mock(User.class);
        when(targetUser.getId()).thenReturn(200L);
        CompanyMembership existingMembership = mock(CompanyMembership.class);

        when(userRepository.findByEmailIgnoreCase("duplicate@hub.com")).thenReturn(Optional.of(targetUser));
        when(companyMembershipRepository.findByUserIdAndCompanyId(200L, 1L)).thenReturn(Optional.of(existingMembership));

        assertThrows(ConflictException.class, () ->
                companyService.addMember(1L, "duplicate@hub.com", Role.RECRUITER)
        );

        verify(companyMembershipRepository, times(0)).save(any(CompanyMembership.class));
    }
// Arrange: Set up a company where the logged-in user is NOT an admin (e.g., RECRUITER, EMPLOYEE)

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMIN"}, mode = EnumSource.Mode.EXCLUDE)
    void addMember_AsNonAdminRole_ThrowsAccessDenied(Role nonAdminRole) {

        givenCompanyWithUserRole(nonAdminRole);

        assertThrows(AccessDeniedException.class, () ->
                companyService.addMember(1L, "newuser@hub.com", Role.EMPLOYEE)
        );

        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
        verify(companyMembershipRepository, never()).save(any(CompanyMembership.class));
    }


    private Company givenCompanyWithUserRole(Role role) {
        Company company = new Company();
        company.setId(1L);
        company.setName("Original Company Name");
        company.setDescription("Original Description");

        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn(42L);

        CompanyMembership membership = mock(CompanyMembership.class);
        when(membership.getRole()).thenReturn(role);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(companyMembershipRepository.findByUserIdAndCompanyId(42L, 1L)).thenReturn(Optional.of(membership));

        return company;
    }

}
