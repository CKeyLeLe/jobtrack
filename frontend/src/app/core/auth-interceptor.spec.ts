import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth-interceptor';
import { AuthStore } from './auth-store';

describe('authInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let token: string | null;
  const expire = vi.fn();

  beforeEach(() => {
    token = 'token-123';
    expire.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthStore, useValue: { token: () => token, expire } },
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('adds the bearer token to API calls', () => {
    http.get('/api/applications').subscribe();

    expect(backend.expectOne('/api/applications').request.headers.get('Authorization')).toBe('Bearer token-123');
  });

  it('never sends the token to another host', () => {
    http.get('https://example.com/api/data').subscribe();

    expect(backend.expectOne('https://example.com/api/data').request.headers.has('Authorization')).toBe(false);
  });

  it('expires the session when the API rejects the token', () => {
    http.get('/api/applications').subscribe({ error: () => undefined });

    backend.expectOne('/api/applications').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(expire).toHaveBeenCalledOnce();
  });

  it('lets the login form handle a 401 sent without a token', () => {
    token = null;
    http.post('/api/auth/login', {}).subscribe({ error: () => undefined });

    backend.expectOne('/api/auth/login').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(expire).not.toHaveBeenCalled();
  });
});
