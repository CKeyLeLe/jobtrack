package com.jobtrack.jobapplication;

import com.jobtrack.support.IntegrationTest;
import com.jobtrack.auth.TokenService;
import com.jobtrack.user.User;
import com.jobtrack.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration : toute l'application (sécurité JWT → controller → service → repository → PostgreSQL).
 */
@IntegrationTest
class JobApplicationApiIntegrationTest {

    private static final String NEW_APPLICATION = """
            {"company": "Acme", "jobTitle": "Java Developer",
             "status": "APPLIED", "appliedOn": "2026-11-02"}
            """;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository users;
    @Autowired
    private TokenService tokens;
    @Autowired
    private JwtEncoder jwtEncoder;

    private String aliceToken;
    private String bobToken;

    @BeforeEach
    void createUsers() {
        User alice = users.save(new User("alice@example.com", "hash"));
        User bob = users.save(new User("bob@example.com", "hash"));
        aliceToken = bearer(tokens.issue(alice.getId(), alice.getEmail()).accessToken());
        bobToken = bearer(tokens.issue(bob.getId(), bob.getEmail()).accessToken());
    }

    @Test
    void fullCrudFlowIsScopedToTheOwner() throws Exception {
        mvc.perform(post("/api/applications").header(HttpHeaders.AUTHORIZATION, aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_APPLICATION))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/applications").header(HttpHeaders.AUTHORIZATION, aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].company").value("Acme"));

        mvc.perform(get("/api/applications").header(HttpHeaders.AUTHORIZATION, bobToken))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /** Cycle de vie complet d'une ressource : création, lecture, modification, suppression. */
    @Test
    void applicationLifecycle() throws Exception {
        String location = mvc.perform(post("/api/applications").header(HttpHeaders.AUTHORIZATION, aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_APPLICATION))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location).header(HttpHeaders.AUTHORIZATION, aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPLIED"));

        mvc.perform(put(location).header(HttpHeaders.AUTHORIZATION, aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"company": "Acme", "jobTitle": "Java Developer", "status": "INTERVIEW",
                                 "appliedOn": "2026-11-02", "followUpOn": "2026-11-20", "notes": "Call on Monday"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEW"))
                .andExpect(jsonPath("$.followUpOn").value("2026-11-20"))
                .andExpect(jsonPath("$.notes").value("Call on Monday"));

        mvc.perform(delete(location).header(HttpHeaders.AUTHORIZATION, aliceToken))
                .andExpect(status().isNoContent());

        mvc.perform(get(location).header(HttpHeaders.AUTHORIZATION, aliceToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void anotherUserCannotUpdateTheApplication() throws Exception {
        String location = mvc.perform(post("/api/applications").header(HttpHeaders.AUTHORIZATION, aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_APPLICATION))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(put(location).header(HttpHeaders.AUTHORIZATION, bobToken)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"company": "Hacked", "jobTitle": "Dev",
                                 "status": "OFFER", "appliedOn": "2026-11-02"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication is required to access this resource."));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String tampered = aliceToken.substring(0, aliceToken.length() - 4) + "AAAA"; // signature altérée

        mvc.perform(get("/api/applications").header(HttpHeaders.AUTHORIZATION, tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("The access token is invalid or expired."));
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        Instant twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("jobtrack").subject("1")
                .issuedAt(twoHoursAgo).expiresAt(twoHoursAgo.plus(1, ChronoUnit.HOURS))
                .build();
        String expired = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mvc.perform(get("/api/applications").header(HttpHeaders.AUTHORIZATION, bearer(expired)))
                .andExpect(status().isUnauthorized());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
