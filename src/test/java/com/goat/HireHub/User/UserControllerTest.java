package com.goat.HireHub.User;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserController;
import com.goat.HireHub.user.UserService;
import com.goat.HireHub.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
