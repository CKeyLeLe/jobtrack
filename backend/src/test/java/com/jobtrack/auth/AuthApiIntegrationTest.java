package com.jobtrack.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrack.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiIntegrationTest {

    private static final String PASSWORD = "correct horse battery";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private UserRepository users;
    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void registerReturnsATokenThatOpensTheApi() throws Exception {
        String token = register("Alice@Example.com", PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn().getResponse().getContentAsString();

        String accessToken = json.readTree(token).get("accessToken").asText();
        Jwt jwt = jwtDecoder.decode(accessToken);
        Long userId = users.findByEmail("alice@example.com").orElseThrow().getId();
        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getClaimAsString("email")).isEqualTo("alice@example.com");

        mvc.perform(get("/api/applications").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void passwordIsStoredAsABcryptHash() throws Exception {
        register("alice@example.com", PASSWORD).andExpect(status().isCreated());

        String stored = users.findByEmail("alice@example.com").orElseThrow().getPasswordHash();
        assertThat(stored).startsWith("{bcrypt}$2a$").doesNotContain(PASSWORD);
    }

    @Test
    void registeringTheSameEmailTwiceIsAConflict() throws Exception {
        register("alice@example.com", PASSWORD).andExpect(status().isCreated());

        register("ALICE@example.com", "another password")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("An account already exists with this email."));
    }

    @Test
    void invalidRegistrationListsTheFields() throws Exception {
        register("not-an-email", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[1].field").value("password"));
    }

    @Test
    void loginWithValidCredentialsReturnsAToken() throws Exception {
        register("alice@example.com", PASSWORD);

        login("  Alice@example.com ", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    /** Mauvais mot de passe et email inconnu : réponses identiques, rien ne fuit. */
    @Test
    void loginFailuresDoNotRevealWhetherTheAccountExists() throws Exception {
        register("alice@example.com", PASSWORD);

        JsonNode wrongPassword = body(login("alice@example.com", "wrong password").andExpect(status().isUnauthorized()));
        JsonNode unknownEmail = body(login("nobody@example.com", PASSWORD).andExpect(status().isUnauthorized()));

        assertThat(wrongPassword.get("detail").asText()).isEqualTo("Invalid email or password.");
        assertThat(unknownEmail.get("detail")).isEqualTo(wrongPassword.get("detail"));
    }

    private ResultActions register(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new Credentials(email, password))));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new Credentials(email, password))));
    }

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private record Credentials(String email, String password) {
    }
}
