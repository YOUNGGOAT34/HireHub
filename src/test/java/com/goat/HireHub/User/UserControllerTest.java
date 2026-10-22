package com.goat.HireHub.User;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.exception.BadRequestException;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserController;
import com.goat.HireHub.user.UserService;
import com.goat.HireHub.user.dto.UserRequest;
import com.goat.HireHub.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest extends ControllerBase {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    UserService userService;


    private static final String VALID_REGISTER = """
            {"firstName":"Kim","lastName":"Otieno","email":"kim@x.com","username":"kim","password":"password123"}
            """;
    private static final String VALID_UPDATE = """
            {"firstName":"Kim","lastName":"Otieno","email":"kim@x.com","username":"kim"}
            """;

    //pagination and search

    @ParameterizedTest
    @ValueSource(strings = {
            "sortBy=password", "sortBy=passwordHash", "direction=banana", "page=-1", "size=0"
    })
    void getUsers_invalidParams_returns400(String query) throws Exception {
        mockMvc.perform(get("/users?" + query))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=-1", "size=0", "size=51"})
    void searchUsers_invalidParams_returns400(String query) throws Exception {
        mockMvc.perform(get("/users/search?" + query))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void getUsers_validParams_buildsPageableCorrectly() throws Exception {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/users")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "lastName")
                        .param("direction", "asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).getAllUsers(captor.capture());
        Pageable p = captor.getValue();
        assertEquals(2, p.getPageNumber());
        assertEquals(5, p.getPageSize());
        assertEquals(Sort.Direction.ASC, p.getSort().getOrderFor("lastName").getDirection());
    }

    @Test
    void getUsers_noParams_appliesDefaults() throws Exception {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/users")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).getAllUsers(captor.capture());
        Pageable p = captor.getValue();
        assertEquals(0, p.getPageNumber());
        assertEquals(10, p.getPageSize());
        assertEquals(Sort.Direction.DESC, p.getSort().getOrderFor("firstName").getDirection());
    }

    @Test
    void getUsers_pageWithTwoUsers_serializesContentAndTotals() throws Exception {
        Page<UserResponse> page = new PageImpl<>(
                List.of(sampleUser(1L, "Kim"), sampleUser(2L, "Amina")),
                PageRequest.of(0, 10), 2);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].password").doesNotExist())
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist());
    }

    @Test
    void searchUsers_validParams_passesFiltersAndPageableToService() throws Exception {
        when(userService.searchUsers(any(), any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/users/search")
                        .param("firstName", "kim")
                        .param("lastName", "otieno")
                        .param("username", "kimo")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).searchUsers(eq("kim"), eq("otieno"), eq("kimo"), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    void searchUsers_noParams_appliesDefaultsAndNullFilters() throws Exception {
        when(userService.searchUsers(any(), any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/users/search")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).searchUsers(isNull(), isNull(), isNull(), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
    }

    @Test
    void searchUsers_noResults_returns200WithEmptyContent() throws Exception {
        when(userService.searchUsers(any(), any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/users/search").param("firstName", "nobody"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    //happy path

    @Test
    void getUserById_whenFound_returns200WithoutPassword() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleUser(1L, "Kim"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("kim@x.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerUser_validBody_returns201AndPassesValuesToService() throws Exception {
        when(userService.registerUser(any(UserRequest.class))).thenReturn(sampleUser(10L, "Kim"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        ArgumentCaptor<UserRequest> captor = ArgumentCaptor.forClass(UserRequest.class);
        verify(userService).registerUser(captor.capture());
        assertEquals("kim@x.com", captor.getValue().email());
        assertEquals("Kim", captor.getValue().firstName());
    }

    @Test
    void updateMe_validBody_returns200() throws Exception {
        when(userService.updateUser(any())).thenReturn(sampleUser(1L, "Kim"));

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("kim@x.com"));
    }

    @Test
    void patchMe_onlyOneFieldSent_returns200() throws Exception {
        when(userService.patchUser(any())).thenReturn(sampleUser(1L, "Amina"));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Amina"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Amina"));
    }

    @Test
    void deleteMe_returns204WithEmptyBody() throws Exception {
        mockMvc.perform(delete("/users/me"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).deleteUserById();
    }

    //validation on writes


    @ParameterizedTest
    @ValueSource(strings = {
            // blank first name
            """
            {"firstName":"","lastName":"Otieno","email":"kim@x.com","username":"kim","password":"password123"}
            """,
            // blank last name
            """
            {"firstName":"Kim","lastName":"","email":"kim@x.com","username":"kim","password":"password123"}
            """,
            // missing email
            """
            {"firstName":"Kim","lastName":"Otieno","username":"kim","password":"password123"}
            """,
            // invalid email
            """
            {"firstName":"Kim","lastName":"Otieno","email":"not-an-email","username":"kim","password":"password123"}
            """,
            // missing password
            """
            {"firstName":"Kim","lastName":"Otieno","email":"kim@x.com","username":"kim"}
            """,
            // password too short (7)
            """
            {"firstName":"Kim","lastName":"Otieno","email":"kim@x.com","username":"kim","password":"1234567"}
            """,
            // malformed JSON
            """
            {"firstName":
            """,
            "{}"
    })
    void registerUser_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void registerUser_passwordTooLong_returns400() throws Exception {
        String body = """
                {"firstName":"Kim","lastName":"Otieno","email":"kim@x.com","username":"kim","password":"%s"}
                """.formatted("a".repeat(73));

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void registerUser_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {"firstName":"","lastName":"Otieno","email":"kim@x.com","username":"kim"}
            """,
            """
            {"firstName":"Kim","lastName":"Otieno","email":"not-an-email","username":"kim"}
            """,
            """
            {"firstName":"Kim","lastName":"Otieno","username":"kim"}
            """
    })
    void updateMe_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {"email":"not-an-email"}
            """,
            """
            {"password":"short"}
            """,
            """
            {"firstName":""}
            """
    })
    void patchMe_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }


    //missing resources and conflicts

    @Test
    void getUserById_whenMissing_returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found with id 99"));
    }

    @Test
    void registerUser_whenEmailTaken_returns409() throws Exception {
        when(userService.registerUser(any(UserRequest.class)))
                .thenThrow(new ConflictException("An account with this email already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("An account with this email already exists"));
    }

    @Test
    void updateMe_whenEmailTaken_returns409() throws Exception {
        when(userService.updateUser(any()))
                .thenThrow(new ConflictException("This email address is already taken by another account"));

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE))
                .andExpect(status().isConflict());
    }

    @Test
    void patchMe_whenEmailTaken_returns409() throws Exception {
        when(userService.patchUser(any()))
                .thenThrow(new ConflictException("This email address is already taken by another account"));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"taken@x.com"}
                                """))
                .andExpect(status().isConflict());
    }

    //security
    @ParameterizedTest
    @ValueSource(strings = {"/users/5"})
    void modifyingAnotherUserById_isNotAllowed(String url) throws Exception {
        mockMvc.perform(delete(url)).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(userService);
    }


    //helpers

    private UserResponse sampleUser(Long id, String firstName) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName("Otieno");
        u.setEmail("kim@x.com");
        u.setUsername("kim");
        return UserResponse.from(u);
    }


}
