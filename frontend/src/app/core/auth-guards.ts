import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from './auth-store';

/**
 * Guards only improve navigation (no flash of a page the user cannot use).
 * They are not security: the API enforces authentication on every request.
 */
export const authGuard: CanActivateFn = () =>
  inject(AuthStore).token() ? true : inject(Router).createUrlTree(['/login']);

/** Login and register pages are pointless for a user who is already logged in. */
export const guestGuard: CanActivateFn = () =>
  inject(AuthStore).token() ? inject(Router).createUrlTree(['/applications']) : true;
