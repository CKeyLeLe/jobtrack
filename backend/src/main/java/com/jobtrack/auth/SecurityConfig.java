package com.jobtrack.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Règles de sécurité HTTP de l'API.
 * <p>
 * API "stateless" : aucune session côté serveur, chaque requête porte son JWT dans
 * l'en-tête Authorization. Le filtre qui lit et vérifie ce token est fourni par
 * Spring Security (resource server) : pas de filtre maison à maintenir.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        var entryPoint = new ProblemDetailAuthenticationEntryPoint(objectMapper);
        http
                // CSRF exploite les cookies envoyés automatiquement par le navigateur. Le JWT est
                // ajouté explicitement par le client dans un en-tête : la protection est inutile ici.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .anyRequest().authenticated()) // refus par défaut : toute nouvelle route est protégée
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(entryPoint))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint));
        return http.build();
    }

    /**
     * Encodeur "délégant" : stocke {@code {bcrypt}$2a$10$...}. Le préfixe indique l'algorithme,
     * ce qui permettra d'en changer plus tard sans invalider les mots de passe existants.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
