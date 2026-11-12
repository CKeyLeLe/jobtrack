package com.jobtrack.jobapplication.dto;

import com.jobtrack.jobapplication.ApplicationStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Teste les règles de validation du DTO directement, sans Spring ni HTTP. */
class JobApplicationRequestValidationTest {

    private static final LocalDate APPLIED_ON = LocalDate.of(2026, 11, 2);

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    void acceptsFollowUpOnOrAfterApplicationDate() {
        assertThat(validator.validate(request(APPLIED_ON))).isEmpty();
        assertThat(validator.validate(request(APPLIED_ON.plusDays(10)))).isEmpty();
        assertThat(validator.validate(request(null))).isEmpty();
    }

    @Test
    void rejectsFollowUpBeforeApplicationOnTheFollowUpField() {
        Set<ConstraintViolation<JobApplicationRequest>> violations = validator.validate(request(APPLIED_ON.minusDays(1)));

        assertThat(violations).singleElement().satisfies(violation -> {
            assertThat(violation.getPropertyPath()).hasToString("followUpOn");
            assertThat(violation.getMessage()).isEqualTo("must not be before appliedOn");
        });
    }

    private static JobApplicationRequest request(LocalDate followUpOn) {
        return new JobApplicationRequest("Acme", "Dev", ApplicationStatus.APPLIED, APPLIED_ON, followUpOn, null);
    }
}
