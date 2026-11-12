package com.jobtrack.jobapplication;

import com.jobtrack.common.NotFoundException;
import com.jobtrack.jobapplication.dto.JobApplicationRequest;
import com.jobtrack.jobapplication.dto.JobApplicationResponse;
import com.jobtrack.user.User;
import com.jobtrack.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

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

    @Mock
    private JobApplicationRepository applications;
    @Mock
    private UserRepository users;
    @InjectMocks
    private JobApplicationService service;

    private final User owner = new User("alice@example.com", "hash");

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
