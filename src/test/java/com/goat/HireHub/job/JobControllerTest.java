package com.goat.HireHub.job;
import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.job.dto.JobResponse;
import com.goat.HireHub.security.JwtService;
import com.goat.HireHub.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.naming.AuthenticationException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest extends ControllerBase {
      @Autowired
    MockMvc mockMvc;
      @MockitoBean
      JobService jobService;


    @ParameterizedTest
    @ValueSource(strings = {
            "sortBy=password", "direction=banana", "page=-1", "size=0", "size=1000"
    })

    void getAll_invalidParams_returns400(String query) throws Exception {
        mockMvc.perform(get("/jobs?" + query))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jobService);
    }



    @ParameterizedTest
    @ValueSource(strings = {
            "sortBy=password", "direction=banana", "page=-1", "size=0", "size=1000",
            "minSalary=-5", "maxSalary=-5", "companyId=0",
            "minSalary=500&maxSalary=100"
    })
    void search_invalidParams_returns400(String query) throws Exception {
        mockMvc.perform(get("/jobs/search?" + query))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jobService);
    }


    @Test
    void getAll_validParams_buildsPageableCorrectly() throws Exception {
        when(jobService.getAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/jobs")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "title")
                        .param("direction", "asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobService).getAll(captor.capture());
        Pageable p = captor.getValue();
        assertEquals(2, p.getPageNumber());
        assertEquals(5, p.getPageSize());
        assertEquals(Sort.Direction.ASC, p.getSort().getOrderFor("title").getDirection());
    }

    @Test
    void getAll_noParams_appliesDefaults() throws Exception {
        when(jobService.getAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/jobs")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobService).getAll(captor.capture());
        Pageable p = captor.getValue();
        assertEquals(0, p.getPageNumber());
        assertEquals(10, p.getPageSize());
        assertEquals(Sort.Direction.DESC, p.getSort().getOrderFor("salary").getDirection());
    }





    @Test
    void search_validParams_passesFiltersAndPageableToService() throws Exception {
        when(jobService.searchJobs(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/jobs/search")
                        .param("title", "java")
                        .param("minSalary", "1000")
                        .param("maxSalary", "5000")
                        .param("companyName", "Safaricom")
                        .param("companyId", "3")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sortBy", "title")
                        .param("direction", "asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobService).searchJobs(
                eq("java"), eq(new BigDecimal("1000")), eq(new BigDecimal("5000")),
                eq("Safaricom"), eq(3L), captor.capture());
        Pageable p = captor.getValue();
        assertEquals(1, p.getPageNumber());
        assertEquals(20, p.getPageSize());
        assertEquals(Sort.Direction.ASC, p.getSort().getOrderFor("title").getDirection());
    }


    @Test
    void search_noParams_appliesDefaultsAndNullFilters() throws Exception {
        when(jobService.searchJobs(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/jobs/search")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobService).searchJobs(
                isNull(), isNull(), isNull(), isNull(), isNull(), captor.capture());
        Pageable p = captor.getValue();
        assertEquals(0, p.getPageNumber());
        assertEquals(10, p.getPageSize());
        assertEquals(Sort.Direction.DESC, p.getSort().getOrderFor("salary").getDirection());
    }


    @Test
    void search_pageWithTwoJobs_serializesContentAndTotals() throws Exception {
        Page<JobResponse> page = new PageImpl<>(
                List.of(sampleJob(1L, "Java Dev"), sampleJob(2L, "Spring Dev")),
                PageRequest.of(0, 10), 2);
        when(jobService.searchJobs(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/jobs/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Java Dev"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }






    @Test
      @WithMockUser
    void getById_WhenJobMissing_returns404WithMessage() throws Exception{
          when(jobService.getById(99L)).thenThrow(new ResourceNotFoundException("Job",99L));
          mockMvc.perform(get("/jobs/99"))
                  .andExpect(status().isNotFound())
                  .andExpect(jsonPath("$.detail").value("Job not found with id 99"));
      }

      @ParameterizedTest
      @WithMockUser
      @ValueSource(strings={"""
                {"title":"","description":"Build APIs","salary":50000,"companyId":1}
                """,
              """
                {"title":"software engineer","description":"","salary":50000,"companyId":1}
                """,
              """
                {"title":"software engineer","description":"Build APIs","salary":50000,"companyId":null}
                """
      })

      void addJob_WhenFieldIsBlank_returns400WithFieldError(String body) throws  Exception{
          mockMvc.perform(post("/jobs")
                          .contentType(MediaType.APPLICATION_JSON)
                           .content(body)
                          )
                  .andExpect(status().isBadRequest())
                  .andExpect(jsonPath("$.errors").isNotEmpty());
          verifyNoInteractions(jobService);
      }

      @Test
      @WithMockUser
      void deleteJob_WhenServiceDeniesAccess_returns403() throws Exception{

          doThrow(new AccessDeniedException("not a member")).when(jobService).deleteById(5L);

          mockMvc.perform(delete("/jobs/5"))
                  .andExpect(status().isForbidden());

      }

    private JobResponse sampleJob(Long id, String title) {
        return new JobResponse(id, title, "Build APIs", new BigDecimal("50000"), 1L, "Safaricom");
    }


}
