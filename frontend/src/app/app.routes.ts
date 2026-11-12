import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth-guards';

/**
 * Each page is lazy-loaded (loadComponent): its code is downloaded only when the user
 * first opens it, which keeps the initial bundle small.
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'applications' },
  {
    path: 'login',
    title: 'Log in · JobTrack',
    canActivate: [guestGuard],
    loadComponent: () => import('./auth/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    title: 'Create account · JobTrack',
    canActivate: [guestGuard],
    loadComponent: () => import('./auth/register-page').then((m) => m.RegisterPage),
  },
  {
    path: '',
    canActivateChild: [authGuard],
    children: [
      {
        path: 'applications',
        title: 'Applications · JobTrack',
        loadComponent: () => import('./applications/application-list').then((m) => m.ApplicationList),
      },
      {
        path: 'applications/new',
        title: 'New application · JobTrack',
        loadComponent: () => import('./applications/application-form').then((m) => m.ApplicationForm),
      },
      {
        path: 'applications/:id/edit',
        title: 'Edit application · JobTrack',
        loadComponent: () => import('./applications/application-form').then((m) => m.ApplicationForm),
      },
      {
        path: 'follow-ups',
        title: 'Follow-ups · JobTrack',
        loadComponent: () => import('./applications/follow-ups').then((m) => m.FollowUps),
      },
    ],
  },
  { path: '**', redirectTo: 'applications' },
];
