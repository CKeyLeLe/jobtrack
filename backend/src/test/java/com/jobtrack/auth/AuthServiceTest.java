package com.jobtrack.auth;

import com.jobtrack.auth.dto.LoginRequest;
import com.jobtrack.auth.dto.RegisterRequest;
import com.jobtrack.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository users;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokens;
    @InjectMocks
    private AuthService authService;

    @Test
    void registerRefusesAnEmailAlreadyTaken() {
        when(users.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Alice@Example.com", "password123")))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(users, never()).saveAndFlush(any());
    }

    /** Deux inscriptions simultanées : la contrainte UNIQUE en base tranche, pas une erreur 500. */
    @Test
    void registerTurnsAUniqueConstraintRaceIntoAConflict() {
        when(users.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("{bcrypt}hash");
        when(users.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_users_email"));

        assertThatThrownBy(() -> authService.register(new RegisterRequest("alice@example.com", "password123")))
                .isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void requestsNeverPrintPasswords() {
        assertThat(new RegisterRequest("alice@example.com", "s3cret-pass")).hasToString(
                "RegisterRequest[email=alice@example.com, password=***]");
        assertThat(new LoginRequest("alice@example.com", "s3cret-pass").toString()).doesNotContain("s3cret");
    }
}
