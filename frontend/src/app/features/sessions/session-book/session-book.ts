import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { AuthStore } from '../../../core/store/auth.store';
import { MentorService, Mentor } from '../../../core/services/mentor.service';
import { Session, SessionService } from '../../../core/services/session.service';
import { SkillService, Skill } from '../../../core/services/skill.service';
import { Observable, startWith, map } from 'rxjs';

@Component({
  selector: 'app-session-book',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatAutocompleteModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatDividerModule
  ],
  templateUrl: './session-book.html',
  styleUrl: './session-book.scss',
})
export class SessionBook implements OnInit {
  readonly store = inject(AuthStore);
  private fb = inject(FormBuilder);
  private sessionService = inject(SessionService);
  private mentorService = inject(MentorService);
  private skillService = inject(SkillService);
  private snackBar = inject(MatSnackBar);
  private route = inject(ActivatedRoute);

  mentors: Mentor[] = [];
  sessions: Session[] = [];
  skills: Skill[] = [];
  isSubmitting = false;
  isLoading = true;
  filteredTopics!: Observable<string[]>;
  minDate = new Date();

  readonly bookingForm = this.fb.group({
    mentorId: [null as number | null, Validators.required],
    topic: ['', [Validators.required, Validators.minLength(3)]],
    sessionDate: [null as Date | null, Validators.required],
    notes: ['']
  });

  async ngOnInit() {
    const userId = this.store.user()?.id ?? 0;
    try {
      const [mentors, skills, sessions] = await Promise.all([
        this.mentorService.getAllMentors(),
        this.skillService.getAllSkills(),
        this.sessionService.getUserSessions(userId)
      ]);
      this.mentors = mentors.filter(m => m.approved !== false && m.available !== false);
      this.skills = skills;
      this.sessions = sessions.sort((a, b) => new Date(b.sessionDate).getTime() - new Date(a.sessionDate).getTime());

      // Pre-select mentor if passed via query params
      this.route.queryParams.subscribe(params => {
        if (params['mentorId']) {
          this.bookingForm.patchValue({ mentorId: +params['mentorId'] });
        }
      });

      // Autocomplete for topics from skills
      this.filteredTopics = this.bookingForm.get('topic')!.valueChanges.pipe(
        startWith(''),
        map(value => this._filterTopics(value || ''))
      );
    } catch (e) {
      console.error('Failed to load session booking data', e);
    } finally {
      this.isLoading = false;
    }
  }

  private _filterTopics(value: string): string[] {
    const lower = value.toLowerCase();
    return this.skills
      .map(s => s.name)
      .filter(name => name.toLowerCase().includes(lower))
      .slice(0, 8);
  }

  get nextSessions(): Session[] {
    return this.sessions.slice(0, 10);
  }

  getStatusClass(status: string): string {
    return status.toLowerCase();
  }

  async submitRequest() {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }

    const { mentorId, topic, sessionDate } = this.bookingForm.value;
    if (!mentorId || !sessionDate) return;

    this.isSubmitting = true;
    try {
      // Backend expects LocalDateTime format (no timezone suffix)
      const date = sessionDate as Date;
      // Set time to 18:30 if no time component (date-only picker)
      if (date.getHours() === 0 && date.getMinutes() === 0) {
        date.setHours(18, 30, 0, 0);
      }
      const pad = (n: number) => String(n).padStart(2, '0');
      const localDateTimeStr = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`;

      const selectedMentorData = this.mentors.find(m => m.id === mentorId);
      
      const newSession = await this.sessionService.requestSession({
        mentorId: mentorId,
        mentorName: selectedMentorData?.name || `Mentor #${mentorId}`,
        learnerId: this.store.user()?.id ?? 0,
        learnerName: this.store.user()?.name || 'Learner',
        sessionDate: localDateTimeStr,
        topic: topic ?? '',
        hourlyRate: selectedMentorData?.hourlyRate || 0
      });

      this.sessions = [newSession, ...this.sessions];
      this.snackBar.open('Session request sent! Awaiting mentor confirmation.', 'OK', { duration: 4000 });
      this.bookingForm.reset({
        mentorId: null,
        topic: '',
        sessionDate: null,
        notes: ''
      });
    } catch (e: any) {
      this.snackBar.open('Failed to send session request: ' + (e.message || 'Please try again.'), 'Dismiss', { duration: 5000 });
    } finally {
      this.isSubmitting = false;
    }
  }

  getMentorName(id: number): string {
    return this.mentors.find(m => m.id === id)?.name ?? `Mentor #${id}`;
  }

  skillLabel(skill: string | number): string {
    if (typeof skill === 'string' && Number.isNaN(Number(skill))) {
      return skill;
    }
    const id = typeof skill === 'number' ? skill : Number(skill);
    return this.skills.find(item => item.id === id)?.name ?? String(skill);
  }

  mentorSkillSummary(mentor: Mentor): string {
    return (mentor.skills || []).slice(0, 3).map(skill => this.skillLabel(skill)).join(', ');
  }
}
