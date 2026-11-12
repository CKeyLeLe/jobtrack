package com.jobtrack.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Lit l'utilisateur courant dans le JWT déjà vérifié par Spring Security.
 * Remplace l'en-tête X-User-Id temporaire : les controllers n'ont pas changé.
 */
@Component
class JwtCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return Long.valueOf(jwtAuthentication.getToken().getSubject());
        }
        // Impossible en pratique : la chaîne de filtres refuse les requêtes sans JWT valide.
        throw new IllegalStateException("No JWT authentication in the security context");
    }
}
