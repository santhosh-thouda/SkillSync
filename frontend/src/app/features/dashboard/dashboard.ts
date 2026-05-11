import { Component, inject, OnInit, signal, computed, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthStore } from '../../core/store/auth.store';
import { MentorService, Mentor } from '../../core/services/mentor.service';
import { SessionService, Session } from '../../core/services/session.service';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, RouterLink, MatCardModule, MatIconModule,
    MatListModule, MatChipsModule, MatButtonModule, MatProgressSpinnerModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  readonly store = inject(AuthStore);
  private mentorService = inject(MentorService);
  private sessionService = inject(SessionService);
  private cdr = inject(ChangeDetectorRef);

  sessions = signal<Session[]>([]);
  mentors = signal<Mentor[]>([]);
  ownMentorProfile = signal<Mentor | null>(null);
  isLoading = signal(false);

  upcomingSessionsCount = computed(() => 
    this.sessions().filter(s => s.status?.toUpperCase() === 'ACCEPTED').length
  );
  pendingSessionsCount = computed(() => 
    this.sessions().filter(s => s.status?.toUpperCase() === 'REQUESTED').length
  );
  completedSessionsCount = computed(() => 
    this.sessions().filter(s => s.status?.toUpperCase() === 'COMPLETED').length
  );

  approvedMentorsCount = signal(0);
  pendingMentorsCount = signal(0);

  readonly learnerFocus = [
    { icon: 'search', label: 'Find a mentor', route: '/mentors' },
    { icon: 'calendar_month', label: 'Book a session', route: '/sessions' },
    { icon: 'groups', label: 'Join a group', route: '/groups' }
  ];

  ngOnInit() {
    this.loadDashboardData();
    
    // Also react to user changes
    toObservable(this.store.user)
      .pipe(filter(u => !!u))
      .subscribe(() => this.loadDashboardData());
  }

  async loadDashboardData() {
    const user = this.store.user();
    if (!user) return;

    const userId = user.id;
    console.log('[Dashboard] Starting data load for user:', userId);
    this.isLoading.set(true);
    this.cdr.detectChanges();

    try {
      if (this.store.isAdmin()) {
        const allMentors = await this.mentorService.getAllMentors();
        this.approvedMentorsCount.set(allMentors.filter(m => m.approved).length);
        this.pendingMentorsCount.set(allMentors.filter(m => !m.approved).length);
        console.log('[Dashboard] Admin load complete');
      } else {
        let sessionData: Session[] = [];
        if (this.store.isMentor()) {
          const mentorProfile = await this.mentorService.getMentorByUserId(userId);
          this.ownMentorProfile.set(mentorProfile);
          
          if (mentorProfile) {
            sessionData = await this.sessionService.getMentorSessions(mentorProfile.id);
          } else {
            sessionData = await this.sessionService.getUserSessions(userId);
          }
        } else {
          sessionData = await this.sessionService.getUserSessions(userId);
          const allMentors = await this.mentorService.getAllMentors();
          this.mentors.set(allMentors.filter(m => m.approved !== false).slice(0, 3));
        }
        this.sessions.set(sessionData);
        console.log('[Dashboard] Sessions loaded:', sessionData.length);
      }
    } catch (error) {
      console.error('[Dashboard] Data load failed:', error);
    } finally {
      this.isLoading.set(false);
      this.cdr.detectChanges();
    }
  }
}
