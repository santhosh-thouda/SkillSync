import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const appRoutes: Routes = [
  // ── Auth ──────────────────────────────────────────────────────
  { 
    path: 'auth', 
    loadComponent: () => import('./layout/public-layout/public-layout').then(m => m.PublicLayout),
    children: [
      { path: '', loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES) }
    ]
  },

  // ── Public Pages ──────────────────────────────────────────────
  {
    path: 'about',
    loadComponent: () => import('./layout/public-layout/public-layout').then(m => m.PublicLayout),
    children: [{ path: '', loadComponent: () => import('./features/public/about/about').then(m => m.About) }]
  },
  {
    path: 'contact',
    loadComponent: () => import('./layout/public-layout/public-layout').then(m => m.PublicLayout),
    children: [{ path: '', loadComponent: () => import('./features/public/contact/contact').then(m => m.Contact) }]
  },
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./layout/public-layout/public-layout').then(m => m.PublicLayout),
    children: [{ path: '', loadComponent: () => import('./features/public/home/home').then(m => m.Home) }]
  },

  // ── Protected App Shell ───────────────────────────────────────
  { 
    path: '',
    loadComponent: () => import('./layout/shell/shell').then(m => m.Shell),
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard').then(m => m.Dashboard) },
      { path: 'mentors', loadChildren: () => import('./features/mentors/mentors.routes').then(m => m.MENTORS_ROUTES) },
      { path: 'sessions', loadChildren: () => import('./features/sessions/sessions.routes').then(m => m.SESSIONS_ROUTES) },
      { path: 'groups', loadChildren: () => import('./features/groups/groups.routes').then(m => m.GROUPS_ROUTES) },
      { path: 'reviews', loadComponent: () => import('./features/reviews/review-list/review-list').then(m => m.ReviewList) },
      { path: 'profile', loadComponent: () => import('./features/profile/profile').then(m => m.Profile) },
      { 
        path: 'admin', 
        loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES),
        canActivate: [roleGuard],
        data: { role: 'ROLE_ADMIN' }
      }
    ]
  },
  { path: '**', loadComponent: () => import('./shared/components/not-found/not-found').then(m => m.NotFound) }
];
