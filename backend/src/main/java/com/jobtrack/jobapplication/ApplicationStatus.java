package com.jobtrack.jobapplication;

import java.util.Set;

/**
 * Étapes du suivi d'une candidature. Stockées en texte ({@code EnumType.STRING}) :
 * réordonner ou insérer une valeur ne corrompt pas les données existantes.
 * Toute nouvelle valeur doit aussi être ajoutée à la contrainte CHECK via une migration.
 */
public enum ApplicationStatus {
    APPLIED,
    INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN;

    /**
     * Candidatures encore en cours : les seules pour lesquelles une relance a du sens.
     * Une candidature refusée ou retirée sort des relances, même si une date était prévue.
     */
    public static final Set<ApplicationStatus> ACTIVE = Set.of(APPLIED, INTERVIEW, OFFER);
}
