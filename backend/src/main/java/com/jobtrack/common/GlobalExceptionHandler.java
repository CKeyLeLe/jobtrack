package com.jobtrack.common;

import com.jobtrack.auth.EmailAlreadyUsedException;
import org.slf4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Comparator;
import java.util.List;

/**
 * Traduit toutes les exceptions en réponses d'erreur au format RFC 7807
 * ({@code application/problem+json}), au même endroit pour toute l'API.
 * <p>
 * Hériter de {@link ResponseEntityExceptionHandler} couvre déjà les erreurs du framework
 * (JSON illisible, type de paramètre invalide, méthode HTTP non supportée,
 * ResponseStatusException...) avec un ProblemDetail. On ajoute :
 * le détail des champs invalides, nos exceptions métier et un filet de sécurité pour le reste.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Un élément de la liste "errors" : quel champ, et pourquoi. */
    public record FieldViolation(String field, String message) {
    }

    /** 400 avec la liste des champs invalides, exploitable par un formulaire. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        List<FieldViolation> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldViolation::field)) // ordre stable pour le client et les tests
                .toList();

        ProblemDetail problem = ex.getBody();
        problem.setTitle("Validation failed");
        problem.setDetail("One or more fields are invalid.");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource not found");
        return problem;
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ProblemDetail handleEmailAlreadyUsed(EmailAlreadyUsedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Email already used");
        return problem;
    }

    /**
     * Échec de connexion. Même message que l'email soit inconnu ou le mot de passe faux :
     * un attaquant ne peut pas savoir quels emails ont un compte.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationFailure(AuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        problem.setTitle("Unauthorized");
        return problem;
    }

    /**
     * Filet de sécurité : toute exception imprévue devient un 500 générique.
     * La trace complète va dans les logs, jamais dans la réponse (pas de fuite
     * d'informations internes : noms de classes, requêtes SQL...).
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setTitle("Internal server error");
        return problem;
    }
}
