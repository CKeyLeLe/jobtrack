package com.jobtrack.jobapplication;

import com.jobtrack.common.NotFoundException;
import com.jobtrack.jobapplication.dto.JobApplicationRequest;
import com.jobtrack.jobapplication.dto.JobApplicationResponse;
import com.jobtrack.user.User;
import com.jobtrack.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Logique métier des candidatures. C'est la frontière transactionnelle : chaque méthode
 * publique s'exécute dans une transaction, et les entités ne sortent jamais d'ici
 * (on renvoie des DTO).
 * <p>
 * Chaque méthode reçoit l'id du propriétaire : le service ne sait rien du HTTP ni de
 * l'authentification, ce qui le rend simple à tester avec Mockito.
 */
@Service
@Transactional(readOnly = true) // défaut pour les lectures : Hibernate saute le dirty checking
public class JobApplicationService {

    private final JobApplicationRepository applications;
    private final UserRepository users;
    private final Clock clock;

    /** Injection par constructeur : dépendances explicites, final, testables sans Spring. */
    public JobApplicationService(JobApplicationRepository applications, UserRepository users, Clock clock) {
        this.applications = applications;
        this.users = users;
        this.clock = clock;
    }

    /** @param status filtre optionnel ; null renvoie toutes les candidatures. */
    public List<JobApplicationResponse> findAll(Long ownerId, ApplicationStatus status) {
        List<JobApplication> result = status == null
                ? applications.findAllByUserIdOrderByAppliedOnDescIdDesc(ownerId)
                : applications.findAllByUserIdAndStatusOrderByAppliedOnDescIdDesc(ownerId, status);
        return toResponses(result);
    }

    /** Relances à faire aujourd'hui ou en retard, pour les candidatures encore actives. */
    public List<JobApplicationResponse> findDueFollowUps(Long ownerId) {
        LocalDate today = LocalDate.now(clock);
        return toResponses(applications.findFollowUpsDueBy(ownerId, ApplicationStatus.ACTIVE, today));
    }

    private static List<JobApplicationResponse> toResponses(List<JobApplication> list) {
        return list.stream().map(JobApplicationResponse::from).toList();
    }

    public JobApplicationResponse findById(Long ownerId, Long id) {
        return JobApplicationResponse.from(findOwned(ownerId, id));
    }

    @Transactional
    public JobApplicationResponse create(Long ownerId, JobApplicationRequest request) {
        User owner = users.findById(ownerId).orElseThrow(() -> new NotFoundException("User", ownerId));
        JobApplication application = new JobApplication(
                owner, request.company().strip(), request.jobTitle().strip(),
                request.status(), request.appliedOn());
        applyOptionalFields(application, request);
        return JobApplicationResponse.from(applications.save(application));
    }

    /**
     * Remplacement complet (sémantique PUT). Pas d'appel à save() : l'entité est "managed"
     * dans la transaction, Hibernate détecte les modifications (dirty checking) et
     * émet l'UPDATE au commit.
     */
    @Transactional
    public JobApplicationResponse update(Long ownerId, Long id, JobApplicationRequest request) {
        JobApplication application = findOwned(ownerId, id);
        application.setCompany(request.company().strip());
        application.setJobTitle(request.jobTitle().strip());
        application.setStatus(request.status());
        application.setAppliedOn(request.appliedOn());
        applyOptionalFields(application, request);
        applications.flush(); // déclenche @PreUpdate pour renvoyer un updatedAt à jour
        return JobApplicationResponse.from(application);
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        applications.delete(findOwned(ownerId, id));
    }

    private JobApplication findOwned(Long ownerId, Long id) {
        return applications.findByIdAndUserId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("Job application", id));
    }

    private static void applyOptionalFields(JobApplication application, JobApplicationRequest request) {
        application.setFollowUpOn(request.followUpOn());
        application.setNotes(request.notes() == null || request.notes().isBlank() ? null : request.notes().strip());
    }
}
