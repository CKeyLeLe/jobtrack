package com.jobtrack.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param password 72 caractères au plus : BCrypt ignore silencieusement tout ce qui
 *                 dépasse 72 octets, on refuse donc explicitement les mots de passe plus longs.
 */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password
) {

    /** Le toString() généré d'un record afficherait le mot de passe dans les logs. */
    @Override
    public String toString() {
        return "RegisterRequest[email=" + email + ", password=***]";
    }
}
