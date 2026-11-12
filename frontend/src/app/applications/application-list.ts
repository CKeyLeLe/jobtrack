import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { toApiError } from '../core/api-error';
import { today } from '../core/dates';
import { ApplicationStatus, JobApplication, STATUS_OPTIONS, statusLabel } from './application';
import { ApplicationsApi } from './applications-api';

const ACTIVE: readonly ApplicationStatus[] = ['APPLIED', 'INTERVIEW', 'OFFER'];

@Component({
  selector: 'app-application-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './application-list.html',
})
export class ApplicationList {
  private readonly api = inject(ApplicationsApi);
  private readonly router = inject(Router);

  /**
   * Bound from ?status=… : the filter lives in the URL, so a filtered list can be
   * bookmarked, shared, and survives a refresh or the back button.
   */
  readonly status = input<ApplicationStatus | undefined>();

  protected readonly applications = signal<JobApplication[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly statusLabel = statusLabel;

  constructor() {
    // Reload whenever the status filter in the URL changes.
    effect(() => {
      const status = this.status();
      untracked(() => this.load(status));
    });
  }

  protected filterBy(status: string): void {
    void this.router.navigate([], { queryParams: { status: status || null } });
  }

  protected isFollowUpDue(application: JobApplication): boolean {
    return (
      application.followUpOn !== null &&
      application.followUpOn <= today() &&
      ACTIVE.includes(application.status)
    );
  }

  protected delete(application: JobApplication): void {
    if (!confirm(`Delete your application to ${application.company}?`)) {
      return;
    }
    this.api.remove(application.id).subscribe({
      next: () => this.applications.update((list) => list.filter((a) => a.id !== application.id)),
      error: (e: unknown) => this.error.set(toApiError(e).message),
    });
  }

  private load(status: ApplicationStatus | undefined): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.list(status).subscribe({
      next: (list) => {
        this.applications.set(list);
        this.loading.set(false);
      },
      error: (e: unknown) => {
        this.error.set(toApiError(e).message);
        this.loading.set(false);
      },
    });
  }
}
