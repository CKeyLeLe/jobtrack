import { booleanAttribute, Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { toApiError } from '../core/api-error';
import { AuthStore } from '../core/auth-store';
import { errorMessage } from '../core/form-errors';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.html',
})
export class LoginPage {
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  /** Bound from the ?expired=true query parameter set by the interceptor. */
  readonly expired = input(false, { transform: booleanAttribute });

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', Validators.required],
    password: ['', Validators.required],
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
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl('/applications'),
      error: (e: unknown) => {
        this.error.set(toApiError(e).message);
        this.submitting.set(false);
      },
    });
  }
}
