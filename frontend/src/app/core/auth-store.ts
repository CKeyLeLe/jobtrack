import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, Observable } from 'rxjs';

export interface Credentials {
  email: string;
  password: string;
}

interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

interface Session {
  token: string;
  email: string;
  expiresAt: number; // epoch milliseconds
}

export const SESSION_STORAGE_KEY = 'jobtrack.session';

/**
 * Holds the current session in a signal, so every component that reads it
 * (header, guards) updates automatically on login and logout.
 *
 * The token is kept in localStorage to survive a page refresh. Trade-off: a script
 * injected through an XSS flaw could read it. Mitigations: Angular escapes every
 * interpolated value by default, the app never uses innerHTML, and tokens expire after one hour.
 */
@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(restoreSession());

  readonly email = computed(() => this.session()?.email ?? null);
  readonly isAuthenticated = computed(() => this.session() !== null);

  login(credentials: Credentials): Observable<void> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(map((r) => this.start(r)));
  }

  register(credentials: Credentials): Observable<void> {
    return this.http.post<AuthResponse>('/api/auth/register', credentials).pipe(map((r) => this.start(r)));
  }

  /** Current token, or null when there is none or it has expired. */
  token(): string | null {
    const current = this.session();
    if (current && Date.now() >= current.expiresAt) {
      this.clear();
      return null;
    }
    return current?.token ?? null;
  }

  logout(): void {
    this.clear();
    void this.router.navigateByUrl('/login');
  }

  /** Called when the API rejects the token (expired or revoked). */
  expire(): void {
    this.clear();
    void this.router.navigate(['/login'], { queryParams: { expired: true } });
  }

  private start(response: AuthResponse): void {
    const session: Session = {
      token: response.accessToken,
      email: readEmailClaim(response.accessToken),
      expiresAt: Date.now() + response.expiresIn * 1000,
    };
    localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
    this.session.set(session);
  }

  private clear(): void {
    localStorage.removeItem(SESSION_STORAGE_KEY);
    this.session.set(null);
  }
}

/**
 * A JWT payload is plain Base64URL JSON: readable without the key. The client only reads it
 * for display; the signature is verified by the server on every request.
 */
function readEmailClaim(token: string): string {
  const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
  return JSON.parse(atob(payload)).email;
}

function restoreSession(): Session | null {
  try {
    const stored = localStorage.getItem(SESSION_STORAGE_KEY);
    const session = stored ? (JSON.parse(stored) as Session) : null;
    if (session && session.expiresAt > Date.now()) {
      return session;
    }
  } catch {
    // corrupted or unavailable storage: start logged out
  }
  localStorage.removeItem(SESSION_STORAGE_KEY);
  return null;
}
