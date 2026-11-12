package com.jobtrack.jobapplication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Toutes les requêtes filtrent par utilisateur : un utilisateur ne doit jamais
 * pouvoir lire ou modifier la candidature d'un autre.
 * <p>
 * Requêtes dérivées du nom de méthode quand elles restent lisibles ; {@code @Query} (JPQL)
 * quand le nom deviendrait illisible.
 */
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /** L'id départage deux candidatures du même jour : ordre stable d'un appel à l'autre. */
    List<JobApplication> findAllByUserIdOrderByAppliedOnDescIdDesc(Long userId);

    List<JobApplication> findAllByUserIdAndStatusOrderByAppliedOnDescIdDesc(Long userId, ApplicationStatus status);

    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);

    /** Relances échues (date passée ou du jour) des candidatures encore actives, les plus urgentes d'abord. */
    @Query("""
            select a from JobApplication a
            where a.user.id = :userId
              and a.status in :statuses
              and a.followUpOn <= :date
            order by a.followUpOn asc, a.id asc
            """)
    List<JobApplication> findFollowUpsDueBy(@Param("userId") Long userId,
                                            @Param("statuses") Collection<ApplicationStatus> statuses,
                                            @Param("date") LocalDate date);
}
