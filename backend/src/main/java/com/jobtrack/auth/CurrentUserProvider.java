package com.jobtrack.auth;

/**
 * Donne l'id de l'utilisateur à l'origine de la requête.
 * <p>
 * Les controllers dépendent de cette interface, pas d'un mécanisme d'authentification
 * précis : quand le JWT arrivera (étape 6), seule l'implémentation changera.
 */
public interface CurrentUserProvider {

    Long currentUserId();
}
