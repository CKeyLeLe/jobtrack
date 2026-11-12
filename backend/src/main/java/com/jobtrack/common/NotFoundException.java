package com.jobtrack.common;

/**
 * Exception métier : la ressource demandée n'existe pas, ou n'appartient pas à l'utilisateur.
 * Les deux cas renvoient volontairement la même réponse 404, pour ne pas révéler
 * l'existence des données d'autrui.
 * <p>
 * Elle ne connaît rien du HTTP : c'est {@link GlobalExceptionHandler} qui la traduit en 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Long id) {
        super(resource + " " + id + " not found");
    }
}
