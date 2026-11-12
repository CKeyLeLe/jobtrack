import { HttpErrorResponse } from '@angular/common/http';

/** Error body returned by the API (RFC 7807 problem details). */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  errors?: { field: string; message: string }[];
}

/** API error reduced to what the UI needs: one message and per-field messages. */
export interface ApiError {
  status: number;
  message: string;
  fieldErrors: Record<string, string>;
}

const UNEXPECTED = 'Something went wrong. Please try again.';

/**
 * Single translation point between the backend error format and the UI.
 * Every screen relies on it, so all errors look and behave the same way.
 */
export function toApiError(error: unknown): ApiError {
  if (!(error instanceof HttpErrorResponse)) {
    return { status: -1, message: UNEXPECTED, fieldErrors: {} };
  }
  if (error.status === 0) {
    return { status: 0, message: 'Cannot reach the server. Check that the API is running.', fieldErrors: {} };
  }
  const body = isProblemDetail(error.error) ? error.error : null;
  const fieldErrors = Object.fromEntries((body?.errors ?? []).map((e) => [e.field, e.message]));
  return { status: error.status, message: body?.detail ?? UNEXPECTED, fieldErrors };
}

function isProblemDetail(value: unknown): value is ProblemDetail {
  return typeof value === 'object' && value !== null && 'status' in value && 'title' in value;
}
