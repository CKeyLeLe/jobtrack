package com.jobtrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de fumée : vérifie que le contexte Spring démarre (configuration, beans, connexion BDD).
 */
@SpringBootTest
@ActiveProfiles("test")
class JobTrackApplicationTests {

    @Test
    void contextLoads() {
    }
}
