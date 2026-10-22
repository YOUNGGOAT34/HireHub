package com.goat.HireHub.company;

import com.goat.HireHub.ControllerBase;
import com.goat.HireHub.company.dto.CompanyResponse;
import com.goat.HireHub.exception.BadRequestException;
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

    private CompanyResponse sampleCompany(Long id, String name) {
        return new CompanyResponse(id, name, "Telecom company");
    }


}
