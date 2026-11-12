package com.jobtrack.jobapplication;

import com.jobtrack.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class JobApplicationRepositoryTest {

    @Autowired
    private JobApplicationRepository repository;

    /** Permet de préparer les données et de vider le cache JPA pour relire depuis la base. */
    @Autowired
    private TestEntityManager em;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = em.persist(new User("alice@example.com", "hash"));
        bob = em.persist(new User("bob@example.com", "hash"));
    }

    @Test
    void persistsAllFieldsAndTimestamps() {
        JobApplication application = new JobApplication(
                alice, "Acme", "Java Developer", ApplicationStatus.APPLIED, LocalDate.of(2026, 11, 2));
        application.setFollowUpOn(LocalDate.of(2026, 11, 16));
        application.setNotes("Referred by a former colleague");
        Long id = em.persistAndFlush(application).getId();
        em.clear(); // force une vraie relecture SQL au lieu du cache de premier niveau

        JobApplication reloaded = repository.findById(id).orElseThrow();

        // Régression : la valeur en mémoire après INSERT doit égaler celle relue en base.
        assertThat(reloaded.getCreatedAt()).isEqualTo(application.getCreatedAt());

        assertThat(reloaded.getCompany()).isEqualTo("Acme");
        assertThat(reloaded.getJobTitle()).isEqualTo("Java Developer");
        assertThat(reloaded.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(reloaded.getAppliedOn()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(reloaded.getFollowUpOn()).isEqualTo(LocalDate.of(2026, 11, 16));
        assertThat(reloaded.getNotes()).isEqualTo("Referred by a former colleague");
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getUpdatedAt()).isEqualTo(reloaded.getCreatedAt());
    }

    @Test
    void listsOnlyTheOwnersApplicationsMostRecentFirst() {
        em.persist(new JobApplication(alice, "Old Corp", "Dev", ApplicationStatus.REJECTED, LocalDate.of(2026, 9, 1)));
        em.persist(new JobApplication(alice, "New Corp", "Dev", ApplicationStatus.APPLIED, LocalDate.of(2026, 11, 1)));
        em.persist(new JobApplication(bob, "Bob Corp", "Dev", ApplicationStatus.APPLIED, LocalDate.of(2026, 10, 1)));
        em.flush();

        assertThat(repository.findAllByUserIdOrderByAppliedOnDesc(alice.getId()))
                .extracting(JobApplication::getCompany)
                .containsExactly("New Corp", "Old Corp");
    }

    @Test
    void doesNotFindAnotherUsersApplicationById() {
        Long bobsApplicationId = em.persistAndFlush(
                new JobApplication(bob, "Bob Corp", "Dev", ApplicationStatus.APPLIED, LocalDate.of(2026, 10, 1))).getId();

        assertThat(repository.findByIdAndUserId(bobsApplicationId, alice.getId())).isEmpty();
        assertThat(repository.findByIdAndUserId(bobsApplicationId, bob.getId())).isPresent();
    }
}
