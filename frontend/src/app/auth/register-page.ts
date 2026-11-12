import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { toApiError } from '../core/api-error';
import { AuthStore } from '../core/auth-store';
import { applyServerErrors, errorMessage } from '../core/form-errors';

@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register-page.html',
})
export class RegisterPage {
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  /** Same limits as RegisterRequest on the backend. */
  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly errorMessage = errorMessage;

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl('/applications'),
      error: (e: unknown) => {
        const apiError = toApiError(e);
        if (!applyServerErrors(this.form, apiError)) {
          this.error.set(apiError.message);
        }
        this.submitting.set(false);
      },
    });
  }
}
