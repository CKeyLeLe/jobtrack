package com.jobtrack.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Démarre un PostgreSQL jetable dans Docker, de la même version qu'en production.
 * <p>
 * {@code @ServiceConnection} : Spring Boot lit l'URL, l'utilisateur et le mot de passe
 * directement depuis le conteneur, aucune propriété spring.datasource.* à écrire.
 * Le conteneur vit aussi longtemps que le contexte Spring, qui est mis en cache et
 * partagé entre les classes de test ayant la même configuration.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));
    }
}
