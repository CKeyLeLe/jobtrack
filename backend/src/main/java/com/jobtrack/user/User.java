package com.jobtrack.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Compte utilisateur. Entité JPA : elle reflète la table {@code users} et ne sort jamais
 * de la couche service (les controllers manipuleront des DTO).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** Hash BCrypt, jamais le mot de passe en clair (étape sécurité). */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Constructeur exigé par JPA ; protected pour qu'on ne crée pas d'objet incomplet. */
    protected User() {
    }

    public User(String email, String passwordHash) {
        this.email = normalizeEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    }

    /**
     * Email stocké en minuscules : "Alice@Mail.com" et "alice@mail.com" désignent
     * le même compte, et la contrainte d'unicité en base reste fiable.
     */
    public static String normalizeEmail(String email) {
        return Objects.requireNonNull(email, "email").trim().toLowerCase(Locale.ROOT);
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
