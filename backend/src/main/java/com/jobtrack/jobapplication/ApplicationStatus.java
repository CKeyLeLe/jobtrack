package com.jobtrack.jobapplication;

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
    WITHDRAWN
}
