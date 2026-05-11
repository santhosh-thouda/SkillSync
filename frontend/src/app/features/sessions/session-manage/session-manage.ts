import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthStore } from '../../../core/store/auth.store';
import { SessionService, Session } from '../../../core/services/session.service';
import { MentorService } from '../../../core/services/mentor.service';

@Component({
    selector: 'app-session-manage',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatCardModule,
        MatIconModule,
        MatChipsModule,
        MatDividerModule,
        MatSnackBarModule,
        MatProgressSpinnerModule,
        MatTabsModule,
        MatTooltipModule
    ],
    templateUrl: './session-manage.html',
    styleUrl: './session-manage.scss'
})
export class SessionManage implements OnInit {
    readonly store = inject(AuthStore);
    private sessionService = inject(SessionService);
    private mentorService = inject(MentorService);
    private snackBar = inject(MatSnackBar);
    private cdr = inject(ChangeDetectorRef);

    sessions: Session[] = [];
    isLoading = false;
    processingId: number | null = null;
    loadMessage = '';

    get requested() { return this.sessions.filter(s => s.status === 'REQUESTED'); }
    get accepted() { return this.sessions.filter(s => s.status === 'ACCEPTED'); }
    get completed() { return this.sessions.filter(s => s.status === 'COMPLETED'); }
    get rejected() { return this.sessions.filter(s => s.status === 'REJECTED' || s.status === 'CANCELLED'); }

    ngOnInit() {
        this.loadSessions();
    }

    loadSessions() {
        this.isLoading = true;
        this.loadMessage = '';
        this.cdr.detectChanges();

        const userId = this.store.user()?.id ?? 0;
        const isMentor = this.store.isMentor();
        console.log('[SessionManage] userId:', userId, 'isMentor:', isMentor);

        if (isMentor) {
            this.mentorService.getMentorByUserId(userId).then(mentorProfile => {
                console.log('[SessionManage] mentorProfile:', mentorProfile);
                if (mentorProfile) {
                    return this.sessionService.getMentorSessions(mentorProfile.id);
                } else {
                    this.sessions = [];
                    this.loadMessage = 'No mentor profile found for this account yet.';
                    return Promise.resolve([]);
                }
            }).then(sessions => {
                if (sessions !== undefined) {
                    this.sessions = sessions;
                }
            }).catch(e => {
                console.error('[SessionManage] load error:', e);
                this.sessions = [];
                this.loadMessage = e?.message || 'Failed to load sessions.';
                this.snackBar.open('Failed to load sessions', 'Dismiss', { duration: 3000 });
            }).finally(() => {
                this.isLoading = false;
                this.loadMessage = this.loadMessage || '';
                this.cdr.detectChanges();
                console.log('[SessionManage] done, isLoading=false, sessions:', this.sessions.length);
            });
        } else {
            this.sessionService.getUserSessions(userId).then(sessions => {
                this.sessions = sessions;
            }).catch(e => {
                console.error('[SessionManage] load error:', e);
                this.sessions = [];
                this.loadMessage = e?.message || 'Failed to load sessions.';
                this.snackBar.open('Failed to load sessions', 'Dismiss', { duration: 3000 });
            }).finally(() => {
                this.isLoading = false;
                this.cdr.detectChanges();
            });
        }
    }

    async accept(session: Session) {
        this.processingId = session.id;
        try {
            const updated = await this.sessionService.acceptSession(session.id);
            this.updateSession(updated);
            this.snackBar.open('Session accepted! Learner has been notified.', 'OK', { duration: 3000 });
        } catch (e: any) {
            this.snackBar.open('Failed to accept: ' + (e.message || 'Try again'), 'Dismiss', { duration: 4000 });
        } finally {
            this.processingId = null;
            this.cdr.detectChanges();
        }
    }

    async reject(session: Session) {
        this.processingId = session.id;
        try {
            const updated = await this.sessionService.rejectSession(session.id);
            this.updateSession(updated);
            this.snackBar.open('Session rejected.', 'OK', { duration: 3000 });
        } catch (e: any) {
            this.snackBar.open('Failed to reject: ' + (e.message || 'Try again'), 'Dismiss', { duration: 4000 });
        } finally {
            this.processingId = null;
            this.cdr.detectChanges();
        }
    }

    async complete(session: Session) {
        this.processingId = session.id;
        try {
            const updated = await this.sessionService.completeSession(session.id);
            this.updateSession(updated);
            this.snackBar.open('Session marked as completed!', 'OK', { duration: 3000 });
        } catch (e: any) {
            this.snackBar.open('Failed: ' + (e.message || 'Try again'), 'Dismiss', { duration: 4000 });
        } finally {
            this.processingId = null;
            this.cdr.detectChanges();
        }
    }

    async cancel(session: Session) {
        this.processingId = session.id;
        try {
            const updated = await this.sessionService.cancelSession(session.id);
            this.updateSession(updated);
            this.snackBar.open('Session cancelled.', 'OK', { duration: 3000 });
        } catch (e: any) {
            this.snackBar.open('Failed to cancel: ' + (e.message || 'Try again'), 'Dismiss', { duration: 4000 });
        } finally {
            this.processingId = null;
            this.cdr.detectChanges();
        }
    }

    private updateSession(updated: Session) {
        const idx = this.sessions.findIndex(s => s.id === updated.id);
        if (idx >= 0) {
            this.sessions = [
                ...this.sessions.slice(0, idx),
                updated,
                ...this.sessions.slice(idx + 1)
            ];
            this.cdr.detectChanges();
        }
    }

    isProcessing(id: number) { return this.processingId === id; }
}
