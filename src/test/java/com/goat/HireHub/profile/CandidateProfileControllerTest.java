package com.goat.HireHub.profile;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.profile.dto.FullProfileResponse;
import com.goat.HireHub.profile.dto.ProfileSummaryResponse;
import com.goat.HireHub.profile.dto.ProfileSummaryUpdateRequest;
import com.goat.HireHub.user.SearchStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CandidateProfileController.class)
class CandidateProfileControllerTest extends ControllerBase {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean CandidateProfileService candidateProfileService;

    private static final String VALID_BODY = """
            {"headline":"Java Developer","summary":"I build APIs","searchStatus":"OPEN_TO_WORK"}
            """;

    @Test
    void getPublicProfile_whenFound_returns200WithProfile() throws Exception {
        when(candidateProfileService.getFullProfileByUserId(1L)).thenReturn(fullProfile(
                "Kim", "Java Developer"));

        mockMvc.perform(get("/users/1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Kim"))
                .andExpect(jsonPath("$.headline").value("Java Developer"))
                .andExpect(jsonPath("$.experiences").isArray())
                .andExpect(jsonPath("$.educations").isArray());
    }

    @Test
    void getPublicProfile_whenUserHasNoProfile_returns200WithNullsAndEmptyLists() throws Exception {
        when(candidateProfileService.getFullProfileByUserId(1L)).thenReturn(fullProfile("Kim", null));

        mockMvc.perform(get("/users/1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Kim"))
                .andExpect(jsonPath("$.headline").doesNotExist())
                .andExpect(jsonPath("$.experiences").isEmpty())
                .andExpect(jsonPath("$.educations").isEmpty());
    }

    @Test
    void getPublicProfile_neverExposesPasswordFields() throws Exception {
        when(candidateProfileService.getFullProfileByUserId(1L)).thenReturn(fullProfile("Kim", "Dev"));

        mockMvc.perform(get("/users/1/profile"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    /*
        A public profile should not reveal the user's email address.

    */

    @Test
    void getPublicProfile_doesNotExposeEmail() throws Exception {
        when(candidateProfileService.getFullProfileByUserId(1L)).thenReturn(fullProfile("Kim", "Dev"));

        mockMvc.perform(get("/users/1/profile"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void getPublicProfile_whenUserMissing_returns404() throws Exception {
        when(candidateProfileService.getFullProfileByUserId(99L))
                .thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(get("/users/99/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found with id 99"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/users/0/profile", "/users/-1/profile", "/users/abc/profile", "/users/me/profile"})
    void getPublicProfile_withBadId_returns400(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(candidateProfileService);
    }

    //put candidate profile
    @Test
    void updateMyProfile_validBody_returns200AndPassesValuesToService() throws Exception {
        when(candidateProfileService.updateMainProfile(any(ProfileSummaryUpdateRequest.class)))
                .thenReturn(new ProfileSummaryResponse(1L, "Java Developer", "I build APIs", SearchStatus.OPEN_TO_WORK));

        mockMvc.perform(put("/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.headline").value("Java Developer"))
                .andExpect(jsonPath("$.summary").value("I build APIs"));

        ArgumentCaptor<ProfileSummaryUpdateRequest> captor =
                ArgumentCaptor.forClass(ProfileSummaryUpdateRequest.class);
        verify(candidateProfileService).updateMainProfile(captor.capture());
        assertEquals("Java Developer", captor.getValue().headline());
        assertEquals("I build APIs", captor.getValue().summary());
        assertEquals(SearchStatus.OPEN_TO_WORK, captor.getValue().searchStatus());
    }

    @Test
    void updateMyProfile_headlineAndSummaryAreIndependent() throws Exception {

        when(candidateProfileService.updateMainProfile(any(ProfileSummaryUpdateRequest.class)))
                .thenReturn(new ProfileSummaryResponse(1L, "Headline text", "Summary text", SearchStatus.OPEN_TO_WORK));

        mockMvc.perform(put("/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"headline":"Headline text","summary":"Summary text","searchStatus":"OPEN_TO_WORK"}
                                """))
                .andExpect(jsonPath("$.headline").value("Headline text"))
                .andExpect(jsonPath("$.summary").value("Summary text"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // search status that doesn't exist
            """
            {"headline":"Dev","summary":"About me","searchStatus":"BANANA"}
            """,
            // malformed JSON
            """
            {"headline":
            """
    })
    void updateMyProfile_unreadableBody_returns400(String body) throws Exception {
        mockMvc.perform(put("/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(candidateProfileService);
    }

    @Test
    void updateMyProfile_headlineTooLong_returns400() throws Exception {
        String body = """
                {"headline":"%s","summary":"About me","searchStatus":"OPEN_TO_WORK"}
                """.formatted("a".repeat(151));   // adjust to your @Size max

        mockMvc.perform(put("/users/me/profile").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(candidateProfileService);
    }

    @Test
    void updateMyProfile_summaryTooLong_returns400() throws Exception {
        String body = """
                {"headline":"Dev","summary":"%s","searchStatus":"OPEN_TO_WORK"}
                """.formatted("a".repeat(5001));   // adjust to your @Size max

        mockMvc.perform(put("/users/me/profile").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(candidateProfileService);
    }

    @Test
    void updateMyProfile_emptyBody_returns400() throws Exception {
        mockMvc.perform(put("/users/me/profile").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(candidateProfileService);
    }


    //helpers
    private FullProfileResponse fullProfile(String firstName, String headline) {
        return new FullProfileResponse(
                1L, firstName, "Otieno",  "kim",
                headline, headline == null ? null : "About me",
                headline == null ? null : SearchStatus.OPEN_TO_WORK,
                List.of(), List.of());
    }



}
