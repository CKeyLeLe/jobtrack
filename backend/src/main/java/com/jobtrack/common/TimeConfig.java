package com.jobtrack.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * L'heure courante est une dépendance injectée, pas un appel statique à LocalDate.now() :
 * un test peut fournir une horloge figée et vérifier "les relances d'aujourd'hui"
 * sans dépendre du jour où il s'exécute.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
