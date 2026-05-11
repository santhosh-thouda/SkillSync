import { Routes } from '@angular/router';
export const SESSIONS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./session-book/session-book').then(m => m.SessionBook) },
  { path: 'manage', loadComponent: () => import('./session-manage/session-manage').then(m => m.SessionManage) }
];
