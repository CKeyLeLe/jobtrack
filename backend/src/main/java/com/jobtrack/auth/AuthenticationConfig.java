package com.jobtrack.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Vérification email + mot de passe déléguée à Spring Security plutôt que codée à la main.
 * DaoAuthenticationProvider compare les hash et se protège contre les attaques temporelles :
 * il calcule un hash même pour un email inconnu, pour que le temps de réponse ne révèle pas
 * si un compte existe.
 */
@Configuration
class AuthenticationConfig {

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
