package com.jobtrack.jobapplication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Toutes les requêtes filtrent par utilisateur : un utilisateur ne doit jamais
 * pouvoir lire ou modifier la candidature d'un autre.
 */
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findAllByUserIdOrderByAppliedOnDesc(Long userId);

    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);
}
