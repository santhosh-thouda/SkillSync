import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../store/auth.store';

export const roleGuard: CanActivateFn = (route, state) => {
  const store = inject(AuthStore);
  const router = inject(Router);
  
  const expectedRole = route.data['role'];
  const userRoles = store.user()?.roles || [];

  if (store.isAuthenticated() && userRoles.includes(expectedRole)) {
    return true;
  }
  
  return router.parseUrl('/dashboard');
};
