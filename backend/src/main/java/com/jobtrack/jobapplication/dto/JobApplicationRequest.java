package com.jobtrack.jobapplication.dto;

import com.jobtrack.jobapplication.ApplicationStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Données envoyées par le client pour créer ou remplacer une candidature (POST et PUT).
 * <p>
 * Un record est immuable et sans code répétitif : idéal pour un DTO. Il ne contient ni id,
 * ni propriétaire, ni dates techniques : le client ne peut pas les imposer.
 */
public record JobApplicationRequest(
        @NotBlank @Size(max = 150) String company,
        @NotBlank @Size(max = 150) String jobTitle,
        @NotNull ApplicationStatus status,
        @NotNull @PastOrPresent LocalDate appliedOn,
        LocalDate followUpOn,
        @Size(max = 2000) String notes
) {

    /**
     * Règle sur plusieurs champs : une relance ne peut pas précéder la candidature.
     * Bean Validation appelle cette méthode car son nom suit la convention "is...".
     */
    @AssertTrue(message = "must not be before appliedOn")
    public boolean isFollowUpOnValid() {
        return followUpOn == null || appliedOn == null || !followUpOn.isBefore(appliedOn);
    }
}
