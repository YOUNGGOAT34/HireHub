package com.goat.HireHub.skill;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.skill.dto.SkillResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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

    private SkillResponse sampleSkill(Long id, String name) {
        Skill skill = new Skill(id, name);
        return SkillResponse.from(skill);
    }



}
