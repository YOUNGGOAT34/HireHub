package com.goat.HireHub.education;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.education.dto.EducationRequest;
import com.goat.HireHub.education.dto.EducationResponse;
import com.goat.HireHub.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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

    //validation on writes

    @ParameterizedTest
    @ValueSource(strings = {
            // blank institution
            """
            {"institutionName":"","startDate":"2016-09-01","isCurrent":false}
            """,
            // missing institution
            """
            {"startDate":"2016-09-01","isCurrent":false}
            """,
            // missing start date
            """
            {"institutionName":"University of Nairobi","isCurrent":false}
            """,
            // wrong date format
            """
            {"institutionName":"University of Nairobi","startDate":"01/09/2016","isCurrent":false}
            """,
            // malformed JSON
            """
            {"institutionName":
            """,
            "{}"
    })
    void addEducation_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(post("/users/me/educations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(educationService);
    }
    @Test
    void addEducation_institutionNameTooLong_returns400() throws Exception {
        String body = """
                {"institutionName":"%s","startDate":"2016-09-01","isCurrent":false}
                """.formatted("a".repeat(256));

        mockMvc.perform(post("/users/me/educations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(educationService);
    }

    @Test
    void addEducation_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/users/me/educations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(educationService);
    }

    @Test
    void addEducation_endDateBeforeStartDate_returns400() throws Exception {
        String body = """
                {"institutionName":"Kenyatta university","startDate":"2020-01-01",
                 "endDate":"2019-01-01","isCurrent":false}
                """;

        mockMvc.perform(post("/users/me/educations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addEducation_currentButHasEndDate_returns400() throws Exception {
        String body = """
                {"institutionName":"University of Nairobi","startDate":"2020-01-01",
                 "endDate":"2022-01-01","isCurrent":true}
                """;

        mockMvc.perform(post("/users/me/educations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            //empty institution and no end date
            """
            {"institutionName":"","startDate":"2016-09-01","isCurrent":false}
            """,
            //is current is false but no end date
            """
            {"startDate":"2016-09-01","isCurrent":false}
            """,
            //no start date
            """
            {"institutionName":"University of Nairobi","isCurrent":false}
            """
    })
    void updateEducation_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(put("/users/me/educations/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(educationService);
    }

    //missing resources

    @Test
    void updateEducation_whenRecordMissing_returns404() throws Exception {
        when(educationService.updateEducation(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Education", 99L));
        mockMvc.perform(put("/users/me/educations/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Education not found with id 99"));
    }

    @Test
    void deleteEducation_whenRecordMissing_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Education", 99L)).when(educationService).deleteEducation(99L);

        mockMvc.perform(delete("/users/me/educations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserEducations_whenUserMissing_returns404() throws Exception {
        when(educationService.getAllEducationsById(99L))
                .thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(get("/users/99/educations"))
                .andExpect(status().isNotFound());
    }

//helpers

    private EducationResponse sampleEducation(Long id, String institution) {
        Education e = new Education();
        e.setId(id);
        e.setInstitutionName(institution);
        e.setDegreeType("BSc");
        e.setFieldOfStudy("Computer Science");
        e.setStartDate(LocalDate.of(2016, 9, 1));
        e.setEndDate(LocalDate.of(2020, 12, 1));
        e.setActivitiesAndSocieties("Coding club");
        return EducationResponse.from(e);
    }




}
