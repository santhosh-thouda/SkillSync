import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { AuthStore } from '../../core/store/auth.store';
import { UserService, UserProfile } from '../../core/services/user.service';
import { MentorService, Mentor } from '../../core/services/mentor.service';
import { SkillService, Skill } from '../../core/services/skill.service';
import { Observable, startWith, map } from 'rxjs';

@Component({
  selector: 'app-profile',
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
    MatSlideToggleModule,
    MatAutocompleteModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {
  readonly store = inject(AuthStore);
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private mentorService = inject(MentorService);
  private skillService = inject(SkillService);
  private snackBar = inject(MatSnackBar);

  userProfile: UserProfile | null = null;
  mentorProfile: Mentor | null = null;
  skills: Skill[] = [];
  filteredSkills!: Observable<string[]>;
  isLoading = true;
  isSaving = false;
  isSavingMentor = false;
  profileMessage = '';
  avatarPreview = '';

  readonly profileForm = this.fb.group({
    name: ['', Validators.required],
    profileImage: [''],
  });

  readonly mentorForm = this.fb.group({
    bio: [''],
    experience: [0, [Validators.min(0)]],
    hourlyRate: [0, [Validators.min(0)]],
    skills: [''],
    available: [true],
    newSkillName: [''],
    newSkillCategory: ['General']
  });

  get roleName() { return this.store.roleLabel(); }

  get profileTitle() {
    if (this.store.isAdmin()) return 'Admin Profile';
    if (this.store.isMentor()) return 'Mentor Profile';
    return 'Learner Profile';
  }

  get profileSubtitle() {
    if (this.store.isAdmin()) return 'Operations access, moderation focus, and platform ownership.';
    if (this.store.isMentor()) return 'Public expertise, session readiness, and teaching presence.';
    return 'Learning goals, preferred skills, and personal progress.';
  }

  get skillsArray(): string[] {
    const val = this.mentorForm.get('skills')?.value ?? '';
    return val ? val.split(',').map((s: string) => s.trim()).filter(Boolean) : [];
  }

  get currentAvatar(): string {
    const img = this.profileForm.get('profileImage')?.value;
    if (img && img.trim()) return img.trim();
    const name = this.profileForm.get('name')?.value || this.store.user()?.name || 'U';
    return `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(name)}&backgroundColor=F43F5E&textColor=ffffff`;
  }

  async ngOnInit() {
    const userId = this.store.user()?.id ?? 0;
    const email = this.store.user()?.email ?? '';

    // Always initialize filteredSkills so the form works even if loading fails
    this.filteredSkills = this.mentorForm.get('skills')!.valueChanges.pipe(
      startWith(''),
      map(v => this._filterSkills(v ?? ''))
    );

    // Pre-populate from auth store immediately — no waiting for API
    this.profileForm.patchValue({
      name: this.store.user()?.name ?? '',
      profileImage: ''
    });

    // Start all requests in parallel immediately
    const profilePromise = this.loadUserProfile(userId, email);
    const skillsPromise = this.skillService.getAllSkills();
    const mentorPromise = this.store.isMentor() ? this.mentorService.getMentorByUserId(userId) : Promise.resolve(null);

    try {
      // Wait for all with a faster timeout (5 seconds)
      const timeout = (ms: number) => new Promise(res => setTimeout(() => res('timeout'), ms));
      
      const results = await Promise.all([
        Promise.race([profilePromise, timeout(5000)]),
        Promise.race([skillsPromise, timeout(5000)]),
        Promise.race([mentorPromise, timeout(5000)])
      ]);

      const user = results[0] === 'timeout' ? null : results[0] as UserProfile;
      const skillList = results[1] === 'timeout' ? [] : results[1] as Skill[];
      const mentor = results[2] === 'timeout' ? null : results[2] as Mentor;

      this.skills = skillList;

      if (user) {
        this.userProfile = user;
        const displayName = user.name || this.store.user()?.name || '';
        this.profileForm.patchValue({ name: displayName, profileImage: user.profileImage ?? '' });
        
        // Sync to global store so other components (sidebar/topbar) reflect the image
        this.store.updateUser({
          name: displayName,
          profileImage: user.profileImage
        });
      }

      if (this.store.isMentor() && mentor) {
        this.mentorProfile = mentor;
        this.mentorForm.patchValue({
          bio: mentor.bio ?? '',
          experience: mentor.experience ?? 0,
          hourlyRate: mentor.hourlyRate ?? 0,
          skills: mentor.skills?.map(skill => this.skillLabel(skill)).join(', ') ?? '',
          available: mentor.available !== false
        });
      }
    } catch (e) {
      console.error('Profile load error:', e);
    } finally {
      this.isLoading = false;
    }
  }

  private async loadUserProfile(userId: number, email: string): Promise<UserProfile> {
    // Try email first — most reliable cross-service identifier
    if (email) {
      try {
        return await this.userService.getUserByEmail(email);
      } catch (emailErr: any) {
        // Only continue if it's a 404 (user doesn't exist yet)
        if (emailErr?.status !== 404) {
          // For other errors (403, 500, network), return a minimal profile from store
          return {
            id: userId,
            email,
            name: this.store.user()?.name ?? email.split('@')[0],
            role: this.store.user()?.roles?.[0] ?? 'ROLE_LEARNER'
          };
        }
      }
    }
    // Try by ID
    try {
      return await this.userService.getUser(userId);
    } catch (idErr: any) {
      // Auto-create if missing, or return store fallback
      if (idErr?.status === 404 || idErr?.status === 403) {
        try {
          const role = this.store.user()?.roles?.[0] ?? 'ROLE_LEARNER';
          const name = this.store.user()?.name ?? email.split('@')[0];
          return await this.userService.createUser({ name, email, role });
        } catch {
          // Creation failed — return minimal profile so page still works
          return { id: userId, email, name: this.store.user()?.name ?? '', role: this.store.user()?.roles?.[0] ?? 'ROLE_LEARNER' };
        }
      }
      // Any other error — return store fallback
      return { id: userId, email, name: this.store.user()?.name ?? '', role: this.store.user()?.roles?.[0] ?? 'ROLE_LEARNER' };
    }
  }

  private _filterSkills(value: string): string[] {
    const parts = value.split(',');
    const current = (parts[parts.length - 1] ?? '').trim().toLowerCase();
    if (!current) return [];
    return this.skills.map(s => s.name).filter(n => n.toLowerCase().includes(current)).slice(0, 8);
  }

  selectSkillSuggestion(skillName: string) {
    const existing = this.mentorForm.get('skills')?.value ?? '';
    const parts = existing.split(',').map((s: string) => s.trim()).filter(Boolean);
    if (!parts.includes(skillName)) parts.push(skillName);
    this.mentorForm.patchValue({ skills: parts.join(', ') });
  }

  skillLabel(skill: string | number): string {
    if (typeof skill === 'string' && Number.isNaN(Number(skill))) return skill;
    const id = typeof skill === 'number' ? skill : Number(skill);
    return this.skills.find(item => item.id === id)?.name ?? String(skill);
  }

  private selectedSkillIds(): number[] {
    const names = this.skillsArray.map(s => s.toLowerCase());
    return this.skills.filter(skill => names.includes(skill.name.toLowerCase())).map(skill => skill.id);
  }

  removeSkill(skillName: string) {
    const parts = this.skillsArray.filter(s => s !== skillName);
    this.mentorForm.patchValue({ skills: parts.join(', ') });
  }

  async saveProfile() {
    if (this.profileForm.invalid) { this.profileForm.markAllAsTouched(); return; }
    this.isSaving = true;
    this.profileMessage = '';
    try {
      const authUserId = this.store.user()?.id ?? 0;
      const email = this.store.user()?.email ?? '';
      const role = this.store.user()?.roles?.[0] ?? 'ROLE_LEARNER';
      const { name, profileImage } = this.profileForm.value;

      // Always use the user-service profile ID, not the auth-service ID
      const profileId = this.userProfile?.id ?? authUserId;

      try {
        this.userProfile = await this.userService.updateUser(profileId, {
          name: name ?? '',
          profileImage: profileImage ?? ''
        });
      } catch (error: any) {
        if (error?.status === 404 || error?.status === 403) {
          // Profile doesn't exist in user-service — create it
          this.userProfile = await this.userService.createUser({
            name: name ?? '',
            email,
            role,
            profileImage: profileImage ?? ''
          });
        } else {
          throw error;
        }
      }

      // Sync changes to global AuthStore
      this.store.updateUser({
        name: this.userProfile.name,
        profileImage: this.userProfile.profileImage
      });

      this.snackBar.open('Profile saved successfully!', 'OK', { duration: 3000 });
    } catch (e: any) {
      this.snackBar.open('Failed to save profile: ' + (e.message || 'Try again'), 'Dismiss', { duration: 4000 });
    } finally {
      this.isSaving = false;
    }
  }

  async saveMentorProfile() {
    this.isSavingMentor = true;
    try {
      const val = this.mentorForm.value;
      if (this.mentorProfile) {
        const updated = await this.mentorService.updateMentor(this.mentorProfile.id, {
          bio: val.bio ?? '',
          experience: val.experience ?? 0,
          hourlyRate: val.hourlyRate ?? 0,
          available: val.available ?? true,
          skills: this.selectedSkillIds()
        });
        this.mentorProfile = updated;
        this.snackBar.open('Mentor profile updated!', 'OK', { duration: 3000 });
        return;
      }

      if (this.store.isMentor()) {
        const reloaded = await this.mentorService.getMentorByUserId(this.store.user()?.id ?? 0);
        if (reloaded) {
          this.mentorProfile = reloaded;
          this.mentorForm.patchValue({
            bio: reloaded.bio ?? '',
            experience: reloaded.experience ?? 0,
            hourlyRate: reloaded.hourlyRate ?? 0,
            skills: reloaded.skills?.map(s => this.skillLabel(s)).join(', ') ?? '',
            available: reloaded.available !== false
          });
          this.snackBar.open('Mentor profile loaded. You can now edit and save.', 'OK', { duration: 3500 });
          return;
        }
      }

      const created = await this.mentorService.apply({
        userId: this.store.user()?.id ?? 0,
        bio: val.bio ?? '',
        experience: val.experience ?? 0,
        hourlyRate: val.hourlyRate ?? 0,
        skills: this.selectedSkillIds()
      });
      this.mentorProfile = created;
      this.snackBar.open('Mentor profile created and sent for approval.', 'OK', { duration: 3500 });
    } catch (e: any) {
      this.snackBar.open('Failed to save mentor profile: ' + (e.message || 'Try again'), 'Dismiss', { duration: 4000 });
    } finally {
      this.isSavingMentor = false;
    }
  }

  async toggleAvailability() {
    if (!this.mentorProfile) return;
    const newVal = !this.mentorProfile.available;
    try {
      await this.mentorService.updateAvailability(this.mentorProfile.id, newVal);
      this.mentorProfile = { ...this.mentorProfile, available: newVal };
      this.snackBar.open(`You are now ${newVal ? 'Available' : 'Unavailable'}`, 'OK', { duration: 2500 });
    } catch (e: any) {
      this.snackBar.open('Failed to update availability', 'Dismiss', { duration: 3000 });
    }
  }

  async createNewSkill() {
    const name = this.mentorForm.get('newSkillName')?.value;
    const category = this.mentorForm.get('newSkillCategory')?.value || 'General';
    if (!name) return;

    try {
      const created = await this.skillService.createSkill({ name, category });
      this.skills.push(created);
      this.mentorForm.patchValue({ newSkillName: '' });
      this.selectSkillSuggestion(created.name);
      this.snackBar.open(`Skill "${created.name}" added to catalog!`, 'OK', { duration: 3000 });
    } catch (e: any) {
      this.snackBar.open('Failed to create skill. It might already exist.', 'Dismiss', { duration: 3000 });
    }
  }
}
