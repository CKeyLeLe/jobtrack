import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthStore, SESSION_STORAGE_KEY } from './auth-store';
import { fakeJwt } from './testing';

describe('AuthStore', () => {
  let http: HttpTestingController;

  function createStore(): AuthStore {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    });
    http = TestBed.inject(HttpTestingController);
    return TestBed.inject(AuthStore);
  }

  beforeEach(() => localStorage.clear());
  afterEach(() => http.verify());

  it('starts a session from the login response and persists it', () => {
    const store = createStore();
    const token = fakeJwt({ sub: '1', email: 'alice@example.com' });

    store.login({ email: 'alice@example.com', password: 'secret123' }).subscribe();
    http.expectOne('/api/auth/login').flush({ accessToken: token, tokenType: 'Bearer', expiresIn: 3600 });

    expect(store.isAuthenticated()).toBe(true);
    expect(store.email()).toBe('alice@example.com');
    expect(store.token()).toBe(token);
    expect(localStorage.getItem(SESSION_STORAGE_KEY)).toContain(token);
  });

  it('restores a valid session after a page reload', () => {
    const session = { token: 'abc', email: 'alice@example.com', expiresAt: Date.now() + 60_000 };
    localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));

    expect(createStore().email()).toBe('alice@example.com');
  });

  it('ignores an expired session', () => {
    const session = { token: 'abc', email: 'alice@example.com', expiresAt: Date.now() - 1 };
    localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));

    const store = createStore();

    expect(store.isAuthenticated()).toBe(false);
    expect(localStorage.getItem(SESSION_STORAGE_KEY)).toBeNull();
  });

  it('ignores corrupted storage instead of crashing', () => {
    localStorage.setItem(SESSION_STORAGE_KEY, '{not json');

    expect(createStore().isAuthenticated()).toBe(false);
  });

  it('logout clears the session', () => {
    localStorage.setItem(
      SESSION_STORAGE_KEY,
      JSON.stringify({ token: 'abc', email: 'a@b.c', expiresAt: Date.now() + 60_000 }),
    );
    const store = createStore();

    store.logout();

    expect(store.token()).toBeNull();
    expect(localStorage.getItem(SESSION_STORAGE_KEY)).toBeNull();
  });
});
