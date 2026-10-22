package com.goat.HireHub.job;
import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest extends ControllerBase {
      @Autowired
    MockMvc mockMvc;
      @MockitoBean
      JobService jobService;


      @Test
      @WithMockUser
    void getById_WhenJobMissing_returns404WithMessage() throws Exception{
          when(jobService.getById(99L)).thenThrow(new ResourceNotFoundException("Job",99L));
          mockMvc.perform(get("/jobs/99"))
                  .andExpect(status().isNotFound())
                  .andExpect(jsonPath("$.detail").value("Job not found with id 99"));
      }

}
