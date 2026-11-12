/** Mirrors the backend ApplicationStatus enum. */
export type ApplicationStatus = 'APPLIED' | 'INTERVIEW' | 'OFFER' | 'REJECTED' | 'WITHDRAWN';

export const STATUS_OPTIONS: readonly { value: ApplicationStatus; label: string }[] = [
  { value: 'APPLIED', label: 'Applied' },
  { value: 'INTERVIEW', label: 'Interview' },
  { value: 'OFFER', label: 'Offer' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'WITHDRAWN', label: 'Withdrawn' },
];

export function statusLabel(status: ApplicationStatus): string {
  return STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status;
}

/** Mirrors JobApplicationResponse. Dates are ISO strings (YYYY-MM-DD). */
export interface JobApplication {
  id: number;
  company: string;
  jobTitle: string;
  status: ApplicationStatus;
  appliedOn: string;
  followUpOn: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

/** Mirrors JobApplicationRequest: the fields a client is allowed to send. */
export type JobApplicationRequest = Pick<
  JobApplication,
  'company' | 'jobTitle' | 'status' | 'appliedOn' | 'followUpOn' | 'notes'
>;

export function toRequest(application: JobApplication): JobApplicationRequest {
  const { company, jobTitle, status, appliedOn, followUpOn, notes } = application;
  return { company, jobTitle, status, appliedOn, followUpOn, notes };
}
