package com.goat.HireHub.skill;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.skill.dto.SkillRequest;
import com.goat.HireHub.skill.dto.SkillResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(SkillController.class)
class SkillControllerTest extends ControllerBase {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    SkillService skillService;

    private static final String VALID_BODY = """
            {"name":"Java"}
            """;

    //get skills of a user

    @Test
    void getUserSkills_returnsListAsJsonArray() throws Exception {
        when(skillService.getSkillsByProfileId(1L))
                .thenReturn(List.of(sampleSkill(1L, "Java"), sampleSkill(2L, "Spring")));

        mockMvc.perform(get("/users/1/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Java"));
    }

    @Test
    void getUserSkills_whenNone_returns200WithEmptyArray() throws Exception {
        when(skillService.getSkillsByProfileId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUserSkills_whenUserMissing_returns404() throws Exception {
        when(skillService.getSkillsByProfileId(99L)).thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(get("/users/99/skills"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found with id 99"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/users/0/skills", "/users/-1/skills", "/users/abc/skills", "/users/me/skills"})
    void getUserSkills_withBadId_returns400(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(skillService);
    }

    //add a skill

    @Test
    void addSkill_validBody_returns201AndPassesValueToService() throws Exception {
        when(skillService.addSkillToProfile(any(SkillRequest.class))).thenReturn(sampleSkill(7L, "Java"));

        mockMvc.perform(post("/users/me/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Java"));

        ArgumentCaptor<SkillRequest> captor = ArgumentCaptor.forClass(SkillRequest.class);
        verify(skillService).addSkillToProfile(captor.capture());
        assertEquals("Java", captor.getValue().name());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // blank name
            """
            {"name":""}
            """,
            // whitespace-only name
            """
            {"name":"   "}
            """,
            // missing name
            "{}",
            // null name
            """
            {"name":null}
            """,
            // malformed JSON
            """
            {"name":
            """
    })
    void addSkill_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(post("/users/me/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(skillService);
    }

    @Test
    void addSkill_nameTooLong_returns400() throws Exception {
        String body = """
                {"name":"%s"}
                """.formatted("a".repeat(101));

        mockMvc.perform(post("/users/me/skills").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(skillService);
    }

    @Test
    void addSkill_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/users/me/skills").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(skillService);
    }

    @Test
    void addSkill_whenAlreadyOnProfile_returns409WithMessage() throws Exception {
        when(skillService.addSkillToProfile(any(SkillRequest.class)))
                .thenThrow(new ConflictException("You have already added the skill 'Java' to your profile."));

        mockMvc.perform(post("/users/me/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("You have already added the skill 'Java' to your profile."));
    }


    private SkillResponse sampleSkill(Long id, String name) {
        Skill skill = new Skill(id, name);
        return SkillResponse.from(skill);
    }



}
