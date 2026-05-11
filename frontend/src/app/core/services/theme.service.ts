import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'skillsync_theme';
  isDark = signal<boolean>(this.loadTheme());

  private loadTheme(): boolean {
    try {
      const saved = localStorage.getItem(this.STORAGE_KEY);
      if (saved !== null) return saved === 'dark';
    } catch { /* ignore */ }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? true;
  }

  toggle() {
    const next = !this.isDark();
    this.isDark.set(next);
    this.apply(next);
    try { localStorage.setItem(this.STORAGE_KEY, next ? 'dark' : 'light'); } catch { /* ignore */ }
  }

  apply(dark: boolean) {
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
  }

  init() {
    this.apply(this.isDark());
  }
}
