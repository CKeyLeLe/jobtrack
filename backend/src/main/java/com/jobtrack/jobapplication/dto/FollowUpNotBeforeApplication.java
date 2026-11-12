package com.jobtrack.jobapplication.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contrainte de classe : la date de relance ne peut pas précéder la date de candidature.
 * Une contrainte sur un seul champ ne peut pas comparer deux champs, d'où une
 * annotation posée sur le record entier, implémentée par {@link FollowUpDateValidator}.
 */
@Documented
@Constraint(validatedBy = FollowUpDateValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FollowUpNotBeforeApplication {

    String message() default "must not be before appliedOn";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
