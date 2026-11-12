package com.jobtrack.jobapplication;

import com.jobtrack.user.User;
import com.jobtrack.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration : toute l'application (controller → service → repository → H2).
 * @Transactional annule les données à la fin de chaque test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JobApplicationApiIntegrationTest {

    private static final String USER_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository users;

    private Long aliceId;
    private Long bobId;

    @BeforeEach
    void createUsers() {
        aliceId = users.save(new User("alice@example.com", "hash")).getId();
        bobId = users.save(new User("bob@example.com", "hash")).getId();
    }

    @Test
    void fullCrudFlowIsScopedToTheOwner() throws Exception {
        mvc.perform(post("/api/applications").header(USER_HEADER, aliceId)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"company": "Acme", "jobTitle": "Java Developer",
                                 "status": "APPLIED", "appliedOn": "2026-11-02"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/applications").header(USER_HEADER, aliceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].company").value("Acme"));

        mvc.perform(get("/api/applications").header(USER_HEADER, bobId))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void anotherUserCannotUpdateTheApplication() throws Exception {
        String location = mvc.perform(post("/api/applications").header(USER_HEADER, aliceId)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"company": "Acme", "jobTitle": "Dev",
                                 "status": "APPLIED", "appliedOn": "2026-11-02"}
                                """))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(put(location).header(USER_HEADER, bobId)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"company": "Hacked", "jobTitle": "Dev",
                                 "status": "OFFER", "appliedOn": "2026-11-02"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestWithoutUserIsUnauthorized() throws Exception {
        mvc.perform(get("/api/applications")).andExpect(status().isUnauthorized());
    }
}
