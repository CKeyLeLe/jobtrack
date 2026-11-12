package com.jobtrack.jobapplication;

import com.jobtrack.auth.CurrentUserProvider;
import com.jobtrack.common.NotFoundException;
import com.jobtrack.jobapplication.dto.JobApplicationRequest;
import com.jobtrack.jobapplication.dto.JobApplicationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest ne charge que la couche web (controller, conversion JSON, validation).
 * Le service est remplacé par un mock : on teste le contrat HTTP, pas la logique métier.
 */
@WebMvcTest(JobApplicationController.class)
class JobApplicationControllerTest {

    private static final Long OWNER_ID = 1L;

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private JobApplicationService service;
    @MockitoBean
    private CurrentUserProvider currentUser;

    @BeforeEach
    void authenticate() {
        when(currentUser.currentUserId()).thenReturn(OWNER_ID);
    }

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        var created = new JobApplicationResponse(42L, "Acme", "Java Developer", ApplicationStatus.APPLIED,
                LocalDate.of(2026, 11, 2), null, null, Instant.now(), Instant.now());
        when(service.create(eq(OWNER_ID), any(JobApplicationRequest.class))).thenReturn(created);

        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("""
                        {"company": "Acme", "jobTitle": "Java Developer",
                         "status": "APPLIED", "appliedOn": "2026-11-02"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/applications/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void createRejectsInvalidPayloadWithoutCallingService() throws Exception {
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("""
                        {"company": " ", "jobTitle": "Dev", "status": "APPLIED",
                         "appliedOn": "2026-11-02", "followUpOn": "2026-10-01"}
                        """))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any(), any());
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(service.findById(OWNER_ID, 99L)).thenThrow(new NotFoundException("Job application", 99L));

        mvc.perform(get("/api/applications/99")).andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/applications/42")).andExpect(status().isNoContent());

        verify(service).delete(OWNER_ID, 42L);
    }
}
