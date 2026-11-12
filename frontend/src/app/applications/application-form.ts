import { Component, computed, DestroyRef, inject, input, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { toApiError } from '../core/api-error';
import { today } from '../core/dates';
import { applyServerErrors, errorMessage } from '../core/form-errors';
import { ApplicationStatus, JobApplicationRequest, STATUS_OPTIONS } from './application';
import { followUpNotBeforeApplied, notInFuture } from './application-validators';
import { ApplicationsApi } from './applications-api';

/** One component for both creation (/applications/new) and edition (/applications/:id/edit). */
@Component({
  selector: 'app-application-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './application-form.html',
})
export class ApplicationForm implements OnInit {
  private readonly api = inject(ApplicationsApi);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  /** Bound from the :id route parameter; undefined when creating. */
  readonly id = input<string>();
  protected readonly isEdit = computed(() => this.id() !== undefined);

  /** Same rules as JobApplicationRequest on the backend. */
  protected readonly form = inject(NonNullableFormBuilder).group({
    company: ['', [Validators.required, Validators.maxLength(150)]],
    jobTitle: ['', [Validators.required, Validators.maxLength(150)]],
    status: ['APPLIED' as ApplicationStatus, Validators.required],
    appliedOn: [today(), [Validators.required, notInFuture]],
    followUpOn: ['', followUpNotBeforeApplied],
    notes: ['', Validators.maxLength(2000)],
  });

  protected readonly loading = signal(false);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly errorMessage = errorMessage;
  protected readonly today = today();

  ngOnInit(): void {
    // The follow-up rule depends on appliedOn: re-check it when that date changes.
    this.form.controls.appliedOn.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.form.controls.followUpOn.updateValueAndValidity());

    const id = this.id();
    if (id !== undefined) {
      this.loading.set(true);
      this.api.get(Number(id)).subscribe({
        next: (application) => {
          this.form.setValue({
            company: application.company,
            jobTitle: application.jobTitle,
            status: application.status,
            appliedOn: application.appliedOn,
            followUpOn: application.followUpOn ?? '',
            notes: application.notes ?? '',
          });
          this.loading.set(false);
        },
        error: (e: unknown) => {
          this.error.set(toApiError(e).message);
          this.loading.set(false);
        },
      });
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: JobApplicationRequest = {
      ...value,
      followUpOn: value.followUpOn || null, // empty date input means "no follow-up"
      notes: value.notes.trim() || null,
    };
    const id = this.id();
    const save$ = id === undefined ? this.api.create(request) : this.api.update(Number(id), request);

    this.submitting.set(true);
    this.error.set(null);
    save$.subscribe({
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
