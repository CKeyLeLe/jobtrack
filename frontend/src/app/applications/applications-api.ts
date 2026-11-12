import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApplicationStatus, JobApplication, JobApplicationRequest } from './application';

/** Thin HTTP client for /api/applications. No state, no UI logic: easy to mock in tests. */
@Injectable({ providedIn: 'root' })
export class ApplicationsApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/applications';

  list(status?: ApplicationStatus | null): Observable<JobApplication[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<JobApplication[]>(this.baseUrl, { params });
  }

  dueFollowUps(): Observable<JobApplication[]> {
    return this.http.get<JobApplication[]>(`${this.baseUrl}/follow-ups`);
  }

  get(id: number): Observable<JobApplication> {
    return this.http.get<JobApplication>(`${this.baseUrl}/${id}`);
  }

  create(request: JobApplicationRequest): Observable<JobApplication> {
    return this.http.post<JobApplication>(this.baseUrl, request);
  }

  update(id: number, request: JobApplicationRequest): Observable<JobApplication> {
    return this.http.put<JobApplication>(`${this.baseUrl}/${id}`, request);
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
