package company;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.company.CompanyMembershipRepository;
import com.goat.HireHub.company.CompanyRepository;
import com.goat.HireHub.company.CompanyService;
import com.goat.HireHub.company.dto.CompanyResponse;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
}
