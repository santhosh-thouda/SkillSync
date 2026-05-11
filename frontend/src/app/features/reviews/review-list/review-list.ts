import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthStore } from '../../../core/store/auth.store';
import { Mentor, MentorService } from '../../../core/services/mentor.service';
import { Review, ReviewService } from '../../../core/services/review.service';
import { Skill, SkillService } from '../../../core/services/skill.service';

@Component({
  selector: 'app-review-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './review-list.html',
  styleUrl: './review-list.scss'
})
export class ReviewList implements OnInit {
  readonly store = inject(AuthStore);
  private fb = inject(FormBuilder);
  private mentorService = inject(MentorService);
  private reviewService = inject(ReviewService);
  private skillService = inject(SkillService);

  mentors: Mentor[] = [];
  reviews: Review[] = [];
  skills: Skill[] = [];
  selectedMentor: Mentor | null = null;
  isLoading = true;
  isFeedLoading = false;
  isSubmitting = false;
  statusMessage = '';
  activeTab: 'my' | 'browse' = 'browse';
  ownMentorId: number | null = null;

  readonly reviewForm = this.fb.group({
    mentorId: [null as number | null, Validators.required],
    rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
    comment: ['', [Validators.required, Validators.minLength(8)]]
  });

  async ngOnInit() {
    this.isLoading = true;
    const userId = this.store.user()?.id;

    // Start all requests in parallel
    const mentorsPromise = this.mentorService.getAllMentors();
    const skillsPromise = this.skillService.getAllSkills();
    
    // 1. Prioritize own profile if user is a mentor
    if (this.store.isMentor() && userId) {
      this.mentorService.getMentorByUserId(userId).then(ownProfile => {
        if (ownProfile) {
          this.ownMentorId = ownProfile.id;
          if (!this.selectedMentor) {
            this.activeTab = 'my';
            this.selectedMentor = ownProfile;
            this.reviewForm.patchValue({ mentorId: ownProfile.id });
            this.loadReviews(ownProfile.id);
          }
        }
      }).catch(e => console.error('Error fetching own profile:', e));
    }

    try {
      // 2. Load general data in the background
      const [mentors, skills] = await Promise.all([mentorsPromise, skillsPromise]);
      this.mentors = mentors.filter(mentor => mentor.approved !== false);
      this.skills = skills;

      // 3. Fallback if no mentor selected yet (e.g. learner or mentor profile lookup still pending)
      if (!this.selectedMentor && this.mentors.length > 0) {
        this.selectedMentor = this.mentors[0];
        this.activeTab = 'browse';
        this.reviewForm.patchValue({ mentorId: this.selectedMentor.id });
        this.loadReviews(this.selectedMentor.id);
      }
    } catch (error) {
      console.error('Initialization error in ReviewList:', error);
      this.statusMessage = 'Failed to load platform data.';
    } finally {
      this.isLoading = false;
    }
  }

  setTab(tab: 'my' | 'browse') {
    this.activeTab = tab;
    if (tab === 'my' && this.ownMentorId) {
      this.onMentorChange(this.ownMentorId);
    }
  }

  get averageRating(): string {
    if (!this.reviews.length) {
      return '0.0';
    }

    const total = this.reviews.reduce((sum, review) => sum + review.rating, 0);
    return (total / this.reviews.length).toFixed(1);
  }

  get ratingStars(): number[] {
    const rating = Math.round(Number(this.averageRating));
    return Array.from({ length: rating }, (_, index) => index + 1);
  }

  async onMentorChange(mentorId: number | null) {
    if (!mentorId) {
      return;
    }

    this.selectedMentor = this.mentors.find((mentor) => mentor.id === mentorId) ?? null;
    await this.loadReviews(mentorId);
  }

  async submitReview() {
    if (!this.store.isLearner()) {
      this.statusMessage = 'Only learners can submit mentor reviews.';
      return;
    }

    if (this.reviewForm.invalid) {
      this.reviewForm.markAllAsTouched();
      return;
    }

    const mentorId = this.reviewForm.value.mentorId;

    if (!mentorId) {
      return;
    }

    this.isSubmitting = true;
    try {
      const review = await this.reviewService.addReview({
        mentorId,
        userId: this.store.user()?.id ?? 0,
        userName: this.store.user()?.name || 'Learner',
        rating: this.reviewForm.value.rating ?? 5,
        comment: this.reviewForm.value.comment ?? ''
      });

      this.reviews = [review, ...this.reviews];
      this.statusMessage = 'Review submitted successfully.';
      this.reviewForm.patchValue({
        rating: 5,
        comment: ''
      });
    } finally {
      this.isSubmitting = false;
    }
  }

  trackByReviewId(_: number, review: Review) {
    return review.id;
  }

  private async loadReviews(mentorId: number) {
    this.isFeedLoading = true;
    this.statusMessage = '';

    try {
      this.reviews = await this.reviewService.getReviewsByMentor(mentorId);
    } finally {
      this.isFeedLoading = false;
    }
  }

  skillLabel(skill: string | number | undefined): string {
    if (skill === undefined || skill === null) {
      return 'General';
    }
    if (typeof skill === 'string' && Number.isNaN(Number(skill))) {
      return skill;
    }
    const id = typeof skill === 'number' ? skill : Number(skill);
    return this.skills.find(item => item.id === id)?.name ?? String(skill);
  }
}
