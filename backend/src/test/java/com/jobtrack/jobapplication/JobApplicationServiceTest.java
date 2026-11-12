package com.jobtrack.jobapplication;

import com.jobtrack.common.NotFoundException;
import com.jobtrack.jobapplication.dto.JobApplicationRequest;
import com.jobtrack.jobapplication.dto.JobApplicationResponse;
import com.jobtrack.user.User;
import com.jobtrack.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitaire pur : pas de Spring, pas de base. Les repositories sont simulés par Mockito,
 * on vérifie uniquement les règles du service. S'exécute en quelques millisecondes.
 */
@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final LocalDate APPLIED_ON = LocalDate.of(2026, 11, 2);

    /** Horloge figée au 12/11/2026 : le résultat ne dépend pas du jour d'exécution du test. */
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);
    private static final Clock FIXED_CLOCK =
            Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);

    @Mock
    private JobApplicationRepository applications;
    @Mock
    private UserRepository users;

    private JobApplicationService service;

    private final User owner = new User("alice@example.com", "hash");

    @BeforeEach
    void createService() {
        service = new JobApplicationService(applications, users, FIXED_CLOCK);
    }

    @Test
    void findAllWithoutStatusReturnsEverything() {
        service.findAll(OWNER_ID, null);

        verify(applications).findAllByUserIdOrderByAppliedOnDescIdDesc(OWNER_ID);
    }

    @Test
    void findAllWithStatusUsesTheFilteredQuery() {
        var interview = new JobApplication(owner, "Acme", "Dev", ApplicationStatus.INTERVIEW, APPLIED_ON);
        when(applications.findAllByUserIdAndStatusOrderByAppliedOnDescIdDesc(OWNER_ID, ApplicationStatus.INTERVIEW))
                .thenReturn(List.of(interview));

        assertThat(service.findAll(OWNER_ID, ApplicationStatus.INTERVIEW))
                .extracting(JobApplicationResponse::status)
                .containsExactly(ApplicationStatus.INTERVIEW);
    }

    @Test
    void dueFollowUpsUseTodayAndOnlyActiveStatuses() {
        service.findDueFollowUps(OWNER_ID);

        verify(applications).findFollowUpsDueBy(
                OWNER_ID, Set.of(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW, ApplicationStatus.OFFER), TODAY);
    }

    @Test
    void createAttachesOwnerAndCleansInput() {
        when(users.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var request = new JobApplicationRequest(
                "  Acme ", " Java Developer ", ApplicationStatus.APPLIED, APPLIED_ON, null, "   ");

        JobApplicationResponse response = service.create(OWNER_ID, request);

        ArgumentCaptor<JobApplication> saved = ArgumentCaptor.forClass(JobApplication.class);
        verify(applications).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(owner);
        assertThat(response.company()).isEqualTo("Acme");
        assertThat(response.jobTitle()).isEqualTo("Java Developer");
        assertThat(response.notes()).as("blank notes are stored as null").isNull();
    }

    @Test
    void createFailsForUnknownOwner() {
        when(users.findById(OWNER_ID)).thenReturn(Optional.empty());
        var request = new JobApplicationRequest("Acme", "Dev", ApplicationStatus.APPLIED, APPLIED_ON, null, null);

        assertThatThrownBy(() -> service.create(OWNER_ID, request)).isInstanceOf(NotFoundException.class);
        verify(applications, never()).save(any());
    }

    @Test
    void findByIdHidesApplicationsOfOtherUsers() {
        when(applications.findByIdAndUserId(42L, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(OWNER_ID, 42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Job application 42 not found");
    }

    @Test
    void updateReplacesAllEditableFields() {
        var existing = new JobApplication(owner, "Acme", "Dev", ApplicationStatus.APPLIED, APPLIED_ON);
        existing.setNotes("old");
        when(applications.findByIdAndUserId(42L, OWNER_ID)).thenReturn(Optional.of(existing));
        var request = new JobApplicationRequest("Acme Corp", "Senior Dev", ApplicationStatus.INTERVIEW,
                APPLIED_ON, APPLIED_ON.plusDays(7), "Interview on Monday");

        JobApplicationResponse response = service.update(OWNER_ID, 42L, request);

        assertThat(response.company()).isEqualTo("Acme Corp");
        assertThat(response.jobTitle()).isEqualTo("Senior Dev");
        assertThat(response.status()).isEqualTo(ApplicationStatus.INTERVIEW);
        assertThat(response.followUpOn()).isEqualTo(APPLIED_ON.plusDays(7));
        assertThat(response.notes()).isEqualTo("Interview on Monday");
    }

    @Test
    void deleteFailsWhenApplicationIsNotOwned() {
        when(applications.findByIdAndUserId(42L, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(OWNER_ID, 42L)).isInstanceOf(NotFoundException.class);
        verify(applications, never()).delete(any());
    }
}
