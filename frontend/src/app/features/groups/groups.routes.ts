import { Routes } from '@angular/router';
export const GROUPS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./group-list/group-list').then(m => m.GroupList) }
];
