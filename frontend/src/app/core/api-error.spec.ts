import { HttpErrorResponse } from '@angular/common/http';
import { toApiError } from './api-error';

describe('toApiError', () => {
  it('maps validation problem details to per-field messages', () => {
    const response = new HttpErrorResponse({
      status: 400,
      error: {
        type: 'about:blank',
        title: 'Validation failed',
        status: 400,
        detail: 'One or more fields are invalid.',
        errors: [
          { field: 'company', message: 'must not be blank' },
          { field: 'followUpOn', message: 'must not be before appliedOn' },
        ],
      },
    });

    expect(toApiError(response)).toEqual({
      status: 400,
      message: 'One or more fields are invalid.',
      fieldErrors: { company: 'must not be blank', followUpOn: 'must not be before appliedOn' },
    });
  });

  it('uses the problem detail as the message', () => {
    const response = new HttpErrorResponse({
      status: 409,
      error: { title: 'Email already used', status: 409, detail: 'An account already exists with this email.' },
    });

    expect(toApiError(response).message).toBe('An account already exists with this email.');
  });

  it('explains when the server cannot be reached', () => {
    expect(toApiError(new HttpErrorResponse({ status: 0 })).message).toContain('Cannot reach the server');
  });

  it('falls back to a generic message for unknown errors', () => {
    expect(toApiError(new Error('boom')).message).toBe('Something went wrong. Please try again.');
    expect(toApiError(new HttpErrorResponse({ status: 502, error: '<html>Bad gateway</html>' })).fieldErrors).toEqual({});
  });
});
