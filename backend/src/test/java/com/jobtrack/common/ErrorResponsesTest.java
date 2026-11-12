package com.jobtrack.common;

import com.jobtrack.auth.CurrentUserProvider;
import com.jobtrack.jobapplication.JobApplicationController;
import com.jobtrack.jobapplication.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.jobtrack.auth.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrat des erreurs de l'API : chaque cas renvoie un ProblemDetail (RFC 7807)
 * avec le bon statut. Le frontend s'appuie sur ce format, il doit donc être testé.
 */
@WebMvcTest(JobApplicationController.class)
@Import(SecurityConfig.class)
@WithMockUser
class ErrorResponsesTest {

    private static final Long OWNER_ID = 1L;

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private JobApplicationService service;
    @MockitoBean
    private CurrentUserProvider currentUser;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void authenticate() {
        when(currentUser.currentUserId()).thenReturn(OWNER_ID);
    }

    @Test
    void invalidFieldsAreListedOneByOne() throws Exception {
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("""
                        {"company": " ", "jobTitle": "Dev", "status": "APPLIED",
                         "appliedOn": "2026-11-02", "followUpOn": "2026-10-01"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/applications"))
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[0].field").value("company"))
                .andExpect(jsonPath("$.errors[0].message").value("must not be blank"))
                .andExpect(jsonPath("$.errors[1].field").value("followUpOn"))
                .andExpect(jsonPath("$.errors[1].message").value("must not be before appliedOn"));
    }

    @Test
    void malformedJsonIsABadRequest() throws Exception {
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unknownStatusValueIsABadRequest() throws Exception {
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("""
                        {"company": "Acme", "jobTitle": "Dev", "status": "HIRED", "appliedOn": "2026-11-02"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void nonNumericIdIsABadRequest() throws Exception {
        mvc.perform(get("/api/applications/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unknownStatusFilterIsABadRequest() throws Exception {
        mvc.perform(get("/api/applications").param("status", "HIRED"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void notFoundExplainsWhichResource() throws Exception {
        when(service.findById(OWNER_ID, 99L)).thenThrow(new NotFoundException("Job application", 99L));

        mvc.perform(get("/api/applications/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Job application 99 not found"));
    }

    /** Erreur levée par un filtre de sécurité, hors du controller : même format malgré tout. */
    @Test
    @WithAnonymousUser
    void anonymousRequestIsUnauthorized() throws Exception {
        mvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.instance").value("/api/applications"));
    }

    @Test
    void unexpectedErrorDoesNotLeakInternals() throws Exception {
        when(service.findAll(OWNER_ID, null)).thenThrow(new IllegalStateException("SELECT * FROM users -- secret"));

        mvc.perform(get("/api/applications"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
                .andExpect(content().string(not(containsString("secret"))));
    }
}
