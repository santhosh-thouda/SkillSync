import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthStore } from '../../core/store/auth.store';
import { ThemeService } from '../../core/services/theme.service';

import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule, RouterOutlet, RouterLink, RouterLinkActive,
    MatIconModule, MatTooltipModule
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss'
})
export class Shell implements OnInit {
  readonly store = inject(AuthStore);
  readonly theme = inject(ThemeService);
  private router = inject(Router);
  private userService = inject(UserService);

  isMobileMenuOpen = signal(false);

  toggleMobileMenu() {
    this.isMobileMenuOpen.update(v => !v);
  }

  closeMobileMenu() {
    this.isMobileMenuOpen.set(false);
  }

  ngOnInit() {
    this.theme.init();
    this.syncProfile();
  }

  private async syncProfile() {
    const user = this.store.user();
    if (!user) return;

    try {
      // Fetch fresh profile data from User Service
      const profile = await this.userService.getUserByEmail(user.email);
      if (profile) {
        this.store.updateUser({
          name: profile.name,
          profileImage: profile.profileImage
        });
      }
    } catch (e) {
      console.warn('[Shell] Could not sync user profile:', e);
    }
  }

  logout() {
    this.store.logout();
    this.router.navigate(['/auth/login']);
  }
}
