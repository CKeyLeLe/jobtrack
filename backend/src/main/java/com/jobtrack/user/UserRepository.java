package com.jobtrack.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data génère l'implémentation à l'exécution : le nom des méthodes suffit
 * à dériver la requête ({@code findByEmail} → {@code WHERE email = ?}).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
