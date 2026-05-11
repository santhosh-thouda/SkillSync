import { signalStore, withState, withComputed, withMethods, patchState, withHooks } from '@ngrx/signals';
import { computed, inject } from '@angular/core';
import { AuthService, User } from '../services/auth.service';

export interface AuthState {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  error: string | null;
}

const STORAGE_KEY = 'skillsync_auth';

function saveToStorage(user: User | null, token: string | null) {
  try {
    if (user && token) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ user, token }));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  } catch { /* ignore storage errors */ }
}

function loadFromStorage(): { user: User | null; token: string | null } {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return { user: null, token: null };
    const parsed = JSON.parse(raw);
    // Validate token is not expired
    if (parsed.token) {
      const parts = parsed.token.split('.');
      if (parts.length === 3) {
        const payload = JSON.parse(atob(parts[1]));
        if (payload.exp && payload.exp * 1000 < Date.now()) {
          localStorage.removeItem(STORAGE_KEY);
          return { user: null, token: null };
        }
      }
    }
    return { user: parsed.user ?? null, token: parsed.token ?? null };
  } catch {
    return { user: null, token: null };
  }
}

export const AuthStore = signalStore(
  { providedIn: 'root' },
  withState<AuthState>(() => {
    const saved = loadFromStorage();
    return {
      user: saved.user,
      token: saved.token,
      isLoading: false,
      error: null
    };
  }),
  withComputed(({ user }) => ({
    isAuthenticated: computed(() => !!user()),
    isAdmin: computed(() => user()?.roles.includes('ROLE_ADMIN') ?? false),
    isMentor: computed(() => user()?.roles.includes('ROLE_MENTOR') ?? false),
    isLearner: computed(() => user()?.roles.includes('ROLE_LEARNER') ?? false),
    roleLabel: computed(() => {
      const roles = user()?.roles ?? [];
      if (roles.includes('ROLE_ADMIN')) return 'Admin';
      if (roles.includes('ROLE_MENTOR')) return 'Mentor';
      return 'Learner';
    }),
  })),
  withMethods((store) => {
    const authService = inject(AuthService);
    return {
      async login(credentials: any) {
        patchState(store, { isLoading: true, error: null });
        try {
          const res = await authService.login(credentials);
          patchState(store, { user: res.user, token: res.token, isLoading: false, error: null });
          saveToStorage(res.user, res.token);
        } catch (error: any) {
          console.error('Login failed in store', error);
          patchState(store, {
            isLoading: false,
            error: error.message || 'Login failed. Please check your connection and credentials.'
          });
        }
      },
      logout() {
        patchState(store, { user: null, token: null, error: null });
        saveToStorage(null, null);
      },
      updateUser(partialUser: Partial<User>) {
        const currentUser = store.user();
        if (currentUser) {
          const updatedUser = { ...currentUser, ...partialUser };
          patchState(store, { user: updatedUser });
          saveToStorage(updatedUser, store.token());
        }
      },
      clearError() {
        patchState(store, { error: null });
      }
    };
  }),
  withHooks({})
);
