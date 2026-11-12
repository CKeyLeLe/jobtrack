package com.jobtrack.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Pas de règle de longueur ici : la connexion ne doit rien révéler de la politique de mot de passe. */
public record LoginRequest(@NotBlank String email, @NotBlank String password) {

    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=***]";
    }
}
