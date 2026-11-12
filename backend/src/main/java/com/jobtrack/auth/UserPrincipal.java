package com.jobtrack.auth;

import com.jobtrack.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adaptateur entre notre entité User et le contrat UserDetails de Spring Security.
 * On garde l'entité indépendante de Spring Security, et l'id reste disponible après
 * l'authentification pour construire le JWT.
 */
public final class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;

    private UserPrincipal(Long id, String email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    static UserPrincipal from(User user) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash());
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    /** Pas de rôles dans JobTrack : chaque utilisateur n'accède qu'à ses propres données. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
