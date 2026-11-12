package com.jobtrack.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception métier : la ressource demandée n'existe pas, ou n'appartient pas à l'utilisateur.
 * Les deux cas renvoient volontairement la même réponse 404, pour ne pas révéler
 * l'existence des données d'autrui.
 * <p>
 * Temporaire : @ResponseStatus suffit pour l'instant ; l'étape 4 centralise la
 * traduction des exceptions en réponses HTTP dans un @RestControllerAdvice.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Long id) {
        super(resource + " " + id + " not found");
    }
}
