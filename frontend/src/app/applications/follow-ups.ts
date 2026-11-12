import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toApiError } from '../core/api-error';
import { addDays, daysBetween, today } from '../core/dates';
import { JobApplication, statusLabel, toRequest } from './application';
import { ApplicationsApi } from './applications-api';

/** Follow-ups due today or overdue, as computed by the backend (active applications only). */
@Component({
  selector: 'app-follow-ups',
  imports: [RouterLink, DatePipe],
  templateUrl: './follow-ups.html',
})
export class FollowUps implements OnInit {
  private readonly api = inject(ApplicationsApi);

  protected readonly followUps = signal<JobApplication[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly statusLabel = statusLabel;

  ngOnInit(): void {
    this.load();
  }

  protected dueLabel(application: JobApplication): string {
    const late = daysBetween(application.followUpOn!, today());
    if (late === 0) return 'Due today';
    return late === 1 ? '1 day overdue' : `${late} days overdue`;
  }

  /** Follow-up sent: clear the date, the application leaves this list. */
  protected markDone(application: JobApplication): void {
    this.reschedule(application, null);
  }

  /** No answer yet: try again in a week. */
  protected snooze(application: JobApplication): void {
    this.reschedule(application, addDays(today(), 7));
  }

  private reschedule(application: JobApplication, followUpOn: string | null): void {
    this.api.update(application.id, { ...toRequest(application), followUpOn }).subscribe({
      next: () => this.followUps.update((list) => list.filter((a) => a.id !== application.id)),
      error: (e: unknown) => this.error.set(toApiError(e).message),
    });
  }

  private load(): void {
    this.api.dueFollowUps().subscribe({
      next: (list) => {
        this.followUps.set(list);
        this.loading.set(false);
      },
      error: (e: unknown) => {
        this.error.set(toApiError(e).message);
        this.loading.set(false);
      },
    });
  }
}
