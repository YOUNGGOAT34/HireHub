package com.goat.HireHub.experience;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.experience.dto.ExperienceRequest;
import com.goat.HireHub.experience.dto.ExperienceResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExperienceController.class)
class ExperienceControllerTest extends ControllerBase {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ExperienceService experienceService;

    private static final String VALID_BODY = """
            {"jobTitle":"Backend Developer","companyName":"Safaricom",
             "startDate":"2021-01-01","endDate":"2023-06-01","isCurrent":false,
             "description":"Built REST APIs"}
            """;

    //happy paths

    @Test
    void addExperience_validBody_returns201AndPassesValuesToService() throws Exception {
        when(experienceService.addExperience(any(ExperienceRequest.class)))
                .thenReturn(sampleExperience(10L, "Backend Developer"));

        mockMvc.perform(post("/users/me/experiences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.jobTitle").value("Backend Developer"));

        ArgumentCaptor<ExperienceRequest> captor = ArgumentCaptor.forClass(ExperienceRequest.class);
        verify(experienceService).addExperience(captor.capture());
        assertEquals("Backend Developer", captor.getValue().jobTitle());
        assertEquals("Safaricom", captor.getValue().companyName());
        assertEquals(LocalDate.of(2021, 1, 1), captor.getValue().startDate());
    }

    @Test
    void addExperience_currentJobWithoutEndDate_returns201() throws Exception {
        when(experienceService.addExperience(any(ExperienceRequest.class)))
                .thenReturn(sampleExperience(11L, "Backend Developer"));

        mockMvc.perform(post("/users/me/experiences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"jobTitle":"Backend Developer","companyName":"Safaricom",
                                  "description":"implemented low latency backend services",
                                 "startDate":"2023-07-01","isCurrent":true}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void updateExperience_validBody_returns200() throws Exception {
        when(experienceService.updateExperience(eq(5L), any()))
                .thenReturn(sampleExperience(5L, "Senior Backend Developer"));

        mockMvc.perform(put("/users/me/experiences/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Senior Backend Developer"));
    }

    @Test
    void deleteExperience_whenAllowed_returns204WithEmptyBody() throws Exception {
        mockMvc.perform(delete("/users/me/experiences/5"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(experienceService).deleteExperienceById(5L);
    }

    @Test
    void getUserExperiences_returnsListAsJsonArray() throws Exception {
        when(experienceService.getAllExperiencesByUserId(1L)).thenReturn(List.of(
                sampleExperience(1L, "Senior Developer"),
                sampleExperience(2L, "Junior Developer")));

        mockMvc.perform(get("/users/1/experiences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].jobTitle").value("Senior Developer"));
    }

    @Test
    void getUserExperiences_whenNone_returns200WithEmptyArray() throws Exception {
        when(experienceService.getAllExperiencesByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/experiences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }


    private ExperienceResponse sampleExperience(Long id, String jobTitle) {
        Experience e = new Experience();
        e.setId(id);
        e.setJobTitle(jobTitle);
        e.setCompanyNameRaw("Safaricom");
        e.setStartDate(LocalDate.of(2021, 1, 1));
        e.setEndDate(LocalDate.of(2023, 6, 1));
        return ExperienceResponse.from(e);
    }


}
