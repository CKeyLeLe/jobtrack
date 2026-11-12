package com.jobtrack.auth.dto;

/**
 * Réponse d'authentification, sur le modèle OAuth2 : le client envoie ensuite
 * {@code Authorization: Bearer <accessToken>} à chaque requête.
 *
 * @param expiresIn durée de validité du token, en secondes
 */
public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
}
