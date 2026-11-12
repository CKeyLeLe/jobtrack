package com.jobtrack.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration JWT typée et validée au démarrage ({@code jobtrack.jwt.*}).
 * Si le secret manque, l'application refuse de démarrer avec un message clair,
 * au lieu d'échouer plus tard à la première connexion.
 *
 * @param secret     clé HMAC encodée en Base64 (au moins 256 bits), jamais versionnée
 * @param expiration durée de validité d'un token
 * @param issuer     émetteur écrit dans le token puis vérifié à la lecture
 */
@Validated
@ConfigurationProperties(prefix = "jobtrack.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotNull Duration expiration,
        @NotBlank String issuer
) {
}
