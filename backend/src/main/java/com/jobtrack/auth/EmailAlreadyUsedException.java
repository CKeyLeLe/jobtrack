package com.jobtrack.auth;

/** Inscription refusée : un compte existe déjà avec cet email (traduit en 409 Conflict). */
public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException() {
        super("An account already exists with this email.");
    }
}
