package com.jobtrack.auth;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Signature et vérification des JWT avec HMAC-SHA256 (HS256) : une seule clé secrète
 * partagée, suffisante quand la même application émet et vérifie les tokens.
 * (Avec plusieurs services, on passerait à RS256 : clé privée pour signer, clé publique pour vérifier.)
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
class JwtConfig {

    private static final int MIN_KEY_BYTES = 32; // 256 bits, minimum exigé pour HS256

    @Bean
    SecretKey jwtSigningKey(JwtProperties properties) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(properties.secret());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("jobtrack.jwt.secret must be Base64-encoded", e);
        }
        if (bytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("jobtrack.jwt.secret must decode to at least 256 bits (32 bytes)");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
    }

    /** Vérifie la signature, l'expiration (exp) et l'émetteur (iss) de chaque token reçu. */
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSigningKey, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }
}
