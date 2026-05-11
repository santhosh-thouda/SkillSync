import { Routes } from '@angular/router';
export const MENTORS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./mentor-list/mentor-list').then(m => m.MentorList) },
  { path: ':id', loadComponent: () => import('./mentor-list/mentor-list').then(m => m.MentorList) }
];
