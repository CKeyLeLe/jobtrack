package com.jobtrack.user;

import com.jobtrack.support.RepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de repository (voir {@link RepositoryTest}) : seule la couche persistance démarre,
 * sur PostgreSQL. Plus rapide qu'un @SpringBootTest complet.
 */
@RepositoryTest
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
