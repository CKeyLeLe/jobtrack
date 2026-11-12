package com.jobtrack.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Méta-annotation des tests d'intégration : application complète + MockMvc + PostgreSQL.
 * <p>
 * Toutes les classes qui l'utilisent ont exactement la même configuration : Spring
 * réutilise donc un seul contexte (et un seul conteneur PostgreSQL) pour toutes.
 * {@code @Transactional} annule les données de chaque test.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgresTestConfig.class)
@Transactional
public @interface IntegrationTest {
}
