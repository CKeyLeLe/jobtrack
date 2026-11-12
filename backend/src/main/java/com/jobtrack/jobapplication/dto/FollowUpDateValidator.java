package com.jobtrack.jobapplication.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Public : Bean Validation instancie la classe par réflexion, hors de Spring aussi. */
public class FollowUpDateValidator implements ConstraintValidator<FollowUpNotBeforeApplication, JobApplicationRequest> {

    @Override
    public boolean isValid(JobApplicationRequest request, ConstraintValidatorContext context) {
        if (request.followUpOn() == null || request.appliedOn() == null
                || !request.followUpOn().isBefore(request.appliedOn())) {
            return true; // les champs absents sont déjà signalés par @NotNull
        }
        // Rattache l'erreur au champ "followUpOn" au lieu de l'objet entier :
        // le frontend peut l'afficher sous le bon champ du formulaire.
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("followUpOn")
                .addConstraintViolation();
        return false;
    }
}
