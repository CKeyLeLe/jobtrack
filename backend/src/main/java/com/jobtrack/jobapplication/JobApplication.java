package com.jobtrack.jobapplication;

import com.jobtrack.common.Timestamps;
import com.jobtrack.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Une candidature envoyée par un utilisateur.
 */
@Entity
@Table(name = "job_applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relation unidirectionnelle : la candidature connaît son propriétaire, mais User n'a pas
     * de liste de candidatures (on les obtient par requête, filtrées et paginées).
     * LAZY : sans cela, @ManyToOne charge l'utilisateur à chaque lecture (EAGER par défaut).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String company;

    @Column(name = "job_title", nullable = false, length = 150)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "applied_on", nullable = false)
    private LocalDate appliedOn;

    /** Date de relance prévue ; null si aucune relance n'est planifiée. */
    @Column(name = "follow_up_on")
    private LocalDate followUpOn;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected JobApplication() {
    }

    public JobApplication(User user, String company, String jobTitle,
                          ApplicationStatus status, LocalDate appliedOn) {
        this.user = Objects.requireNonNull(user, "user");
        this.company = Objects.requireNonNull(company, "company");
        this.jobTitle = Objects.requireNonNull(jobTitle, "jobTitle");
        this.status = Objects.requireNonNull(status, "status");
        this.appliedOn = Objects.requireNonNull(appliedOn, "appliedOn");
    }

    @PrePersist
    void onCreate() {
        createdAt = Timestamps.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Timestamps.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = Objects.requireNonNull(company, "company");
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = Objects.requireNonNull(jobTitle, "jobTitle");
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public LocalDate getAppliedOn() {
        return appliedOn;
    }

    public void setAppliedOn(LocalDate appliedOn) {
        this.appliedOn = Objects.requireNonNull(appliedOn, "appliedOn");
    }

    public LocalDate getFollowUpOn() {
        return followUpOn;
    }

    public void setFollowUpOn(LocalDate followUpOn) {
        this.followUpOn = followUpOn;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
