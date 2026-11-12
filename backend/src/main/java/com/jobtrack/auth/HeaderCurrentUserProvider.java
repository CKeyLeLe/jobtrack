package com.jobtrack.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * TEMPORAIRE, non sécurisé : lit l'id utilisateur dans l'en-tête {@code X-User-Id}.
 * N'importe quel client peut s'y faire passer pour un autre utilisateur.
 * Sert uniquement à développer le CRUD avant l'authentification ; remplacé à l'étape 6
 * par une implémentation fondée sur le JWT.
 */
@Component
class HeaderCurrentUserProvider implements CurrentUserProvider {

    static final String HEADER = "X-User-Id";

    private final HttpServletRequest request;

    /** Spring injecte un proxy qui pointe vers la requête HTTP en cours. */
    HeaderCurrentUserProvider(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Long currentUserId() {
        String value = request.getHeader(HEADER);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) { // couvre aussi l'en-tête absent (null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid " + HEADER + " header");
        }
    }
}
