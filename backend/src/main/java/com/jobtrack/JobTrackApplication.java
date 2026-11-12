package com.jobtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'API.
 * <p>
 * {@code @SpringBootApplication} active l'auto-configuration et le scan des composants
 * dans ce package et ses sous-packages : c'est pourquoi cette classe est à la racine
 * ({@code com.jobtrack}) et les fonctionnalités dans des sous-packages.
 */
@SpringBootApplication
public class JobTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobTrackApplication.class, args);
    }
}
