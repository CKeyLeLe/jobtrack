package com.jobtrack.auth;

import com.jobtrack.auth.dto.AuthResponse;
import com.jobtrack.auth.dto.LoginRequest;
import com.jobtrack.auth.dto.RegisterRequest;
import com.jobtrack.user.User;
import com.jobtrack.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
    }

    /**
     * Crée le compte et connecte directement l'utilisateur (renvoie un token).
     * <p>
     * Volontairement sans @Transactional : si deux inscriptions identiques arrivent en même
     * temps, toutes deux passent existsByEmail, mais la contrainte UNIQUE en base rejette la
     * seconde. saveAndFlush force l'INSERT ici pour pouvoir intercepter cette violation.
     */
    public AuthResponse register(RegisterRequest request) {
        String email = User.normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }
        User user;
        try {
            user = users.saveAndFlush(new User(email, passwordEncoder.encode(request.password())));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyUsedException();
        }
        return tokens.issue(user.getId(), user.getEmail());
    }

    /** Lève une AuthenticationException (traduite en 401) si l'email ou le mot de passe est faux. */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return tokens.issue(principal.getId(), principal.getEmail());
    }
}
