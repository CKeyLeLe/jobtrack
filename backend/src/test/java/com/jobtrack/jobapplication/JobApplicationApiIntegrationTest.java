package com.jobtrack.jobapplication;

import com.jobtrack.auth.TokenService;
import com.jobtrack.user.User;
import com.jobtrack.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration : toute l'application (sécurité JWT → controller → service → repository → H2).
 * @Transactional annule les données à la fin de chaque test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
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
