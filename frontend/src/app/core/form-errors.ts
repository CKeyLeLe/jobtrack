import { AbstractControl, FormGroup } from '@angular/forms';
import { ApiError } from './api-error';

/**
 * Message to show under a field, or null. Errors appear only once the user has touched
 * the field (or tried to submit), never while the form is still pristine.
 */
export function errorMessage(control: AbstractControl | null): string | null {
  if (!control?.errors || !control.touched) {
    return null;
  }
  const errors = control.errors;
  if (errors['server']) return errors['server'];
  if (errors['required']) return 'This field is required.';
  if (errors['email']) return 'Enter a valid email address.';
  if (errors['minlength']) return `Use at least ${errors['minlength'].requiredLength} characters.`;
  if (errors['maxlength']) return `Use at most ${errors['maxlength'].requiredLength} characters.`;
  if (errors['futureDate']) return 'The date cannot be in the future.';
  if (errors['beforeApplication']) return 'The follow-up cannot be before the application date.';
  return 'Invalid value.';
}

/**
 * Puts the server's field errors (400 "errors" list) on the matching form controls,
 * so they appear exactly like client-side errors. Returns false if none matched.
 */
export function applyServerErrors(form: FormGroup, error: ApiError): boolean {
  let applied = false;
  for (const [field, message] of Object.entries(error.fieldErrors)) {
    const control = form.get(field);
    if (control) {
      control.setErrors({ server: message });
      control.markAsTouched();
      applied = true;
    }
  }
  return applied;
}
