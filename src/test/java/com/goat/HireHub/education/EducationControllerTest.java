package com.goat.HireHub.education;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.education.dto.EducationRequest;
import com.goat.HireHub.education.dto.EducationResponse;
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


@WebMvcTest(EducationController.class)
class EducationControllerTest extends ControllerBase {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    EducationService educationService;

    private static final String VALID_BODY = """
            {"institutionName":"University of Nairobi","degreeType":"BSc",
             "fieldOfStudy":"Computer Science","startDate":"2016-09-01",
             "endDate":"2020-12-01","isCurrent":false,
             "activitiesAndSocieties":"Coding club"}
            """;

    //happy paths
    @Test
    void addEducation_validBody_returns201AndPassesValuesToService() throws Exception {
        when(educationService.addEducation(any(EducationRequest.class)))
                .thenReturn(sampleEducation(10L, "University of Nairobi"));

        mockMvc.perform(post("/users/me/educations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.institutionName").value("University of Nairobi"));

        ArgumentCaptor<EducationRequest> captor = ArgumentCaptor.forClass(EducationRequest.class);
        verify(educationService).addEducation(captor.capture());
        assertEquals("University of Nairobi", captor.getValue().institutionName());
        assertEquals(LocalDate.of(2016, 9, 1), captor.getValue().startDate());
    }

    @Test
    void updateEducation_validBody_returns200() throws Exception {
        when(educationService.updateEducation(eq(5L), any()))
                .thenReturn(sampleEducation(5L, "Strathmore University"));

        mockMvc.perform(put("/users/me/educations/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionName").value("Strathmore University"));
    }

    @Test
    void deleteEducation_whenAllowed_returns204WithEmptyBody() throws Exception {
        mockMvc.perform(delete("/users/me/educations/5"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(educationService).deleteEducation(5L);
    }

    @Test
    void getUserEducations_returnsListAsJsonArray() throws Exception {
        when(educationService.getAllEducationsById(1L)).thenReturn(List.of(
                sampleEducation(1L, "University of Nairobi"),
                sampleEducation(2L, "Strathmore University")));

        mockMvc.perform(get("/users/1/educations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].institutionName").value("University of Nairobi"));
    }

    @Test
    void getUserEducations_whenNone_returns200WithEmptyArray() throws Exception {
        when(educationService.getAllEducationsById(1L)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/educations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }


    private EducationResponse sampleEducation(Long id, String institution) {
        Education e = new Education();
        e.setId(id);
        e.setInstitutionName(institution);
        e.setDegreeType("BSc");
        e.setFieldOfStudy("Computer Science");
        e.setStartDate(LocalDate.of(2016, 9, 1));
        e.setEndDate(LocalDate.of(2020, 12, 1));
        return EducationResponse.from(e);
    }




}
