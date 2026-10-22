package com.goat.HireHub.company;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.company.dto.CompanyRequest;
import com.goat.HireHub.company.dto.CompanyResponse;
import com.goat.HireHub.exception.BadRequestException;
import com.goat.HireHub.exception.ResourceNotFoundException;
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


@WebMvcTest(CompanyController.class)
class CompanyControllerTest extends ControllerBase{
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CompanyService companyService;

    private static final String VALID_COMPANY = """
            {"name":"Safaricom","description":"Telecom company"}
            """;
    private static final String VALID_INVITE = """
            {"email":"jane@x.com","role":"RECRUITER"}
            """;



    //pagination and search
    @ParameterizedTest
    @ValueSource(strings = {
            "sortBy=password", "direction=banana", "page=-1", "size=0", "size=101"
    })
    void getAll_invalidParams_returns400(String query) throws Exception {
        mockMvc.perform(get("/companies?" + query))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "sortBy=password", "direction=banana", "page=-1", "size=0", "size=101"
    })
    void search_invalidParams_returns400(String query) throws Exception {
        mockMvc.perform(get("/companies/search?" + query))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @Test
    void getAll_validParams_buildsPageableCorrectly() throws Exception {
        when(companyService.getAllCompanies(any(Pageable.class))).thenReturn(Page.empty());
        mockMvc.perform(get("/companies")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(companyService).getAllCompanies(captor.capture());
        Pageable p = captor.getValue();
        assertEquals(2, p.getPageNumber());
        assertEquals(5, p.getPageSize());
        assertEquals(Sort.Direction.ASC, p.getSort().getOrderFor("name").getDirection());
    }

    @Test
    void getAll_noParams_appliesDefaults() throws Exception {
        when(companyService.getAllCompanies(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/companies")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(companyService).getAllCompanies(captor.capture());
        Pageable p = captor.getValue();
        assertEquals(0, p.getPageNumber());
        assertEquals(10, p.getPageSize());
        assertEquals(Sort.Direction.DESC, p.getSort().getOrderFor("name").getDirection());
    }

    @Test
    void search_validParams_passesNameAndPageableToService() throws Exception {
        when(companyService.searchCompany(any(), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/companies/search")
                        .param("name", "safa")
                        .param("page", "1")
                        .param("size", "20")
                        .param("direction", "asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(companyService).searchCompany(eq("safa"), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.ASC, captor.getValue().getSort().getOrderFor("name").getDirection());
    }

    @Test
    void search_noParams_appliesDefaultsAndNullName() throws Exception {
        when(companyService.searchCompany(any(), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/companies/search")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(companyService).searchCompany(isNull(), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
    }

    @Test
    void search_pageWithTwoCompanies_serializesContentAndTotals() throws Exception {
        Page<CompanyResponse> page = new PageImpl<>(
                List.of(sampleCompany(1L, "Safaricom"), sampleCompany(2L, "Equity")),
                PageRequest.of(0, 10), 2);
        when(companyService.searchCompany(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/companies/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Safaricom"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void search_noResults_returns200WithEmptyContent() throws Exception {
        when(companyService.searchCompany(any(), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/companies/search").param("name", "nothing-matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getAll_whenServiceRejects_returns400WithMessage() throws Exception {
        when(companyService.getAllCompanies(any(Pageable.class)))
                .thenThrow(new BadRequestException("bad things"));

        mockMvc.perform(get("/companies"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("bad things"));
    }

    //happy paths

    @Test
    void getById_whenFound_returns200WithCompany() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(sampleCompany(1L, "Safaricom"));
        mockMvc.perform(get("/companies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Safaricom"));
    }

    @Test
    void addCompany_validBody_returns201AndPassesValuesToService() throws Exception {
        when(companyService.addCompany(any(CompanyRequest.class)))
                .thenReturn(sampleCompany(10L, "Safaricom"));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_COMPANY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        ArgumentCaptor<CompanyRequest> captor = ArgumentCaptor.forClass(CompanyRequest.class);
        verify(companyService).addCompany(captor.capture());
        assertEquals("Safaricom", captor.getValue().name());
        assertEquals("Telecom company", captor.getValue().description());
    }

    @Test
    void updateCompany_validBody_returns200() throws Exception {
        when(companyService.updateCompany(eq(5L), any())).thenReturn(sampleCompany(5L, "Safaricom"));

        mockMvc.perform(put("/companies/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_COMPANY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Safaricom"));
    }

    @Test
    void patchCompany_onlyOneFieldSent_returns200() throws Exception {
        when(companyService.patchCompany(eq(5L), any())).thenReturn(sampleCompany(5L, "New Name"));

        mockMvc.perform(patch("/companies/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New Name"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void deleteCompany_whenAllowed_returns204WithEmptyBody() throws Exception {
        mockMvc.perform(delete("/companies/5"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(companyService).deleteById(5L);
    }

    @Test
    void getMembers_returns200() throws Exception {
        when(companyService.getMembers(eq(1L), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/companies/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void addMember_validBody_returns201AndPassesValuesToService() throws Exception {
        mockMvc.perform(post("/companies/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INVITE))
                .andExpect(status().isCreated());

        verify(companyService).addMember(1L, "jane@x.com", Role.RECRUITER);
    }

    //validation on writes

    @ParameterizedTest
    @ValueSource(strings = {
            // blank name
            """
            {"name":"","description":"Telecom"}
            """,
            // missing name
            """
            {"description":"Telecom"}
            """,
            // malformed JSON
            """
            {"name":
            """,
            "{}"
    })
    void addCompany_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @Test
    void addCompany_nameTooLong_returns400() throws Exception {
        String body = """
                {"name":"%s","description":"Telecom"}
                """.formatted("a".repeat(101));

        mockMvc.perform(post("/companies").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @Test
    void addCompany_descriptionTooLong_returns400() throws Exception {
        String body = """
                {"name":"Safaricom","description":"%s"}
                """.formatted("a".repeat(1001));

        mockMvc.perform(post("/companies").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @Test
    void addCompany_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/companies").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {"name":"","description":"Telecom"}
            """,
            """
            {"description":"Telecom"}
            """
    })
    void updateCompany_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(put("/companies/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {"name":""}
            """,
            """
            {"description":""}
            """
    })
    void patchCompany_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(patch("/companies/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // missing email
            """
            {"role":"RECRUITER"}
            """,
            // blank email
            """
            {"email":"","role":"RECRUITER"}
            """,
            // not an email
            """
            {"email":"not-an-email","role":"RECRUITER"}
            """,
            // missing role
            """
            {"email":"jane@x.com"}
            """,
            // role that doesn't exist
            """
            {"email":"jane@x.com","role":"BOSS"}
            """
    })
    void addMember_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(post("/companies/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(companyService);
    }


    //missing resources


    @Test
    void getById_whenCompanyMissing_returns404WithMessage() throws Exception {
        when(companyService.getCompanyById(99L)).thenThrow(new ResourceNotFoundException("Company", 99L));

        mockMvc.perform(get("/companies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Company not found with id 99"));
    }

    @Test
    void updateCompany_whenCompanyMissing_returns404() throws Exception {
        when(companyService.updateCompany(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Company", 99L));

        mockMvc.perform(put("/companies/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_COMPANY))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchCompany_whenCompanyMissing_returns404() throws Exception {
        when(companyService.patchCompany(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Company", 99L));

        mockMvc.perform(patch("/companies/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New Name"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCompany_whenCompanyMissing_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Company", 99L)).when(companyService).deleteById(99L);

        mockMvc.perform(delete("/companies/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMembers_whenCompanyMissing_returns404() throws Exception {
        when(companyService.getMembers(eq(99L), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Company", 99L));

        mockMvc.perform(get("/companies/99/members"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMember_whenUserEmailUnknown_returns404() throws Exception {
        when(companyService.addMember(eq(1L), any(), any()))
                .thenThrow(new ResourceNotFoundException("User", 0L));

        mockMvc.perform(post("/companies/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_INVITE))
                .andExpect(status().isNotFound());
    }




    private CompanyResponse sampleCompany(Long id, String name) {
        return new CompanyResponse(id, name, "Telecom company");
    }


}
