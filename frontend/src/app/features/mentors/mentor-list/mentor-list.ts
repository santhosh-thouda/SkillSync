import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSliderModule } from '@angular/material/slider';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MentorService, Mentor } from '../../../core/services/mentor.service';
import { SkillService, Skill } from '../../../core/services/skill.service';
import { AuthStore } from '../../../core/store/auth.store';

@Component({
  selector: 'app-mentor-list',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule,
    MatFormFieldModule, MatInputModule, MatIconModule, MatButtonModule,
    MatSelectModule, MatSliderModule, MatSlideToggleModule,
    MatAutocompleteModule, MatProgressSpinnerModule, MatChipsModule, MatTooltipModule
  ],
  templateUrl: './mentor-list.html',
  styleUrl: './mentor-list.scss'
})
export class MentorList implements OnInit {
  private fb = inject(FormBuilder);
  private mentorService = inject(MentorService);
  private skillService = inject(SkillService);
  private router = inject(Router);
  readonly store = inject(AuthStore);

  mentors: Mentor[] = [];
  skills: Skill[] = [];
  filteredSkillNames: string[] = [];
  isLoading = true;

  filterForm: FormGroup = this.fb.group({
    search: [''],
    skill: [''],
    minRating: [0],
    maxPrice: [0],
    available: [false]
  });

  async ngOnInit() {
    const [, skillList] = await Promise.all([
      this.fetchMentors({}),
      this.skillService.getAllSkills()
    ]);
    this.skills = skillList;

    // Reactive skill autocomplete
    this.filterForm.get('skill')!.valueChanges.subscribe(v => {
      const lower = (v ?? '').toLowerCase();
      this.filteredSkillNames = lower
        ? this.skills.map(s => s.name).filter(n => n.toLowerCase().includes(lower)).slice(0, 8)
        : [];
    });

    // Debounced filter search
    this.filterForm.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(async (filters) => {
      await this.fetchMentors(filters);
    });
  }

  async fetchMentors(filters: any) {
    this.isLoading = true;
    try {
      const params: any = {};
      if (filters.skill) params['skill'] = filters.skill;
      if (filters.minRating) params['rating'] = filters.minRating;
      if (filters.maxPrice) params['maxPrice'] = filters.maxPrice;
      if (filters.available) params['available'] = true;
      if (filters.search) params['search'] = filters.search;

      this.mentors = await this.mentorService.searchMentors(params);
    } finally {
      this.isLoading = false;
    }
  }

  bookSession(mentor: Mentor) {
    this.router.navigate(['/sessions'], { queryParams: { mentorId: mentor.id } });
  }

  clearFilters() {
    this.filterForm.reset({ search: '', skill: '', minRating: 0, maxPrice: 0, available: false });
  }

  ratingStars(rating: number | undefined): number[] {
    return Array.from({ length: Math.round(rating ?? 0) }, (_, i) => i);
  }

  skillLabel(skill: string | number): string {
    if (typeof skill === 'string' && Number.isNaN(Number(skill))) {
      return skill;
    }
    const id = typeof skill === 'number' ? skill : Number(skill);
    return this.skills.find(item => item.id === id)?.name ?? String(skill);
  }
}
