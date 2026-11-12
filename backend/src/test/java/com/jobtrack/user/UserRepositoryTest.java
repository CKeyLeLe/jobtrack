package com.jobtrack.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @DataJpaTest ne démarre que la couche persistance (JPA, Flyway, repositories) :
 * plus rapide qu'un @SpringBootTest complet. Chaque test est annulé (rollback) à la fin.
 * replace = NONE : on garde notre H2 configurée en mode PostgreSQL au lieu de la base par défaut.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void storesEmailInLowercaseAndFindsIt() {
        User saved = userRepository.saveAndFlush(new User("  Alice@Example.COM ", "hash"));

        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(userRepository.findByEmail("alice@example.com")).isPresent();
        assertThat(userRepository.existsByEmail("bob@example.com")).isFalse();
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        userRepository.saveAndFlush(new User("alice@example.com", "hash"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(new User("ALICE@example.com", "other")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
