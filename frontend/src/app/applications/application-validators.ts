import { ValidatorFn } from '@angular/forms';
import { today } from '../core/dates';

/**
 * Client-side copies of the backend rules, for instant feedback. The backend stays the
 * authority: these can be bypassed, its validation cannot.
 * ISO dates (YYYY-MM-DD) compare correctly as plain strings.
 */
export const notInFuture: ValidatorFn = (control) =>
  control.value && control.value > today() ? { futureDate: true } : null;

/** Mirrors @FollowUpNotBeforeApplication. Re-run it when appliedOn changes. */
export const followUpNotBeforeApplied: ValidatorFn = (control) => {
  const appliedOn: string | undefined = control.parent?.get('appliedOn')?.value;
  const followUpOn: string | undefined = control.value;
  return followUpOn && appliedOn && followUpOn < appliedOn ? { beforeApplication: true } : null;
};
