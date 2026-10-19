package job;


import com.goat.HireHub.company.CompanyService;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.JobRepository;
import com.goat.HireHub.job.JobService;
import com.goat.HireHub.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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

}
