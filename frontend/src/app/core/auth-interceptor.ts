import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthStore } from './auth-store';

/**
 * Adds the JWT to every call to our API, in one place instead of in each service.
 *
 * Only relative /api/ URLs get the token: it must never leak to a third-party host.
 * A 401 on a request that carried a token means the session is over: log out and
 * send the user to the login page. A 401 without a token (wrong password) is left
 * to the login form.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthStore);
  const token = request.url.startsWith('/api/') ? auth.token() : null;
  const authorized = token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;

  return next(authorized).pipe(
    catchError((error: unknown) => {
      if (token && error instanceof HttpErrorResponse && error.status === 401) {
        auth.expire();
      }
      return throwError(() => error);
    }),
  );
};
