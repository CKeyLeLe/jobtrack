package com.jobtrack.jobapplication.dto;

import com.jobtrack.jobapplication.ApplicationStatus;
import com.jobtrack.jobapplication.JobApplication;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Représentation d'une candidature renvoyée par l'API. Le contrat JSON est découplé
 * de l'entité : on peut renommer une colonne sans casser le frontend, et rien de
 * l'utilisateur propriétaire (email, hash) ne fuit dans la réponse.
 */
public record JobApplicationResponse(
        Long id,
        String company,
        String jobTitle,
        ApplicationStatus status,
        LocalDate appliedOn,
        LocalDate followUpOn,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {

    public static JobApplicationResponse from(JobApplication application) {
        return new JobApplicationResponse(
                application.getId(),
                application.getCompany(),
                application.getJobTitle(),
                application.getStatus(),
                application.getAppliedOn(),
                application.getFollowUpOn(),
                application.getNotes(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }
}
