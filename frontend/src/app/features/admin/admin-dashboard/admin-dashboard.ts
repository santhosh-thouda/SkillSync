import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MentorService, Mentor } from '../../../core/services/mentor.service';
import { SkillService, Skill } from '../../../core/services/skill.service';
import { UserService, UserProfile } from '../../../core/services/user.service';
import { Group, GroupService } from '../../../core/services/group.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatInputModule,
    MatFormFieldModule,
    MatTabsModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatDividerModule,
    MatTooltipModule
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboard implements OnInit {
  private mentorService = inject(MentorService);
  private skillService = inject(SkillService);
  private userService = inject(UserService);
  private groupService = inject(GroupService);
  private snackBar = inject(MatSnackBar);
  private fb = new FormBuilder();

  skillForm = this.fb.group({ name: [''], category: [''] });

  pendingMentors: Mentor[] = [];
  approvedMentors: Mentor[] = [];
  allMentors: Mentor[] = [];
  allUsers: UserProfile[] = [];
  groups: Group[] = [];
  skills: Skill[] = [];
  isLoading = true;
  processingId: number | null = null;

  readonly mentorDisplayedColumns = ['name', 'experience', 'skills', 'actions'];
  readonly skillDisplayedColumns = ['id', 'name', 'category', 'actions'];
  readonly userDisplayedColumns = ['id', 'name', 'email', 'role', 'actions'];
  readonly groupDisplayedColumns = ['id', 'name', 'members', 'createdBy', 'actions'];

  get totalMentors() { return this.allMentors.length; }
  get liveMentors() { return this.approvedMentors.length; }
  get pendingCount() { return this.pendingMentors.length; }
  get totalLearners() { return this.allUsers.filter(u => u.role?.includes('LEARNER')).length; }
  get totalUsers() { return this.allUsers.length; }
  get newRegistrations() {
    const since = Date.now() - 24 * 60 * 60 * 1000;
    return this.allUsers.filter(user => user.createdAt && new Date(user.createdAt).getTime() >= since).length;
  }

  async ngOnInit() { await this.loadData(); }

  async loadData() {
    this.isLoading = true;
    try {
      const [mentors, skills, users, groups] = await Promise.all([
        this.mentorService.getAllMentors(),
        this.skillService.getAllSkills(),
        this.userService.getAllUsers(),
        this.groupService.getAllGroups()
      ]);
      this.allMentors = mentors;
      this.pendingMentors = mentors.filter(m => !m.approved);
      this.approvedMentors = mentors.filter(m => m.approved);
      this.skills = skills;
      this.allUsers = users;
      this.groups = groups;
    } catch (e) {
      console.error('Failed to load admin data', e);
      this.snackBar.open('Failed to load admin data', 'Dismiss', { duration: 3000 });
    } finally {
      this.isLoading = false;
    }
  }

  async approveMentor(id: number) {
    this.processingId = id;
    try {
      await this.mentorService.approveMentor(id);
      await this.loadData();
      this.snackBar.open('Mentor approved!', 'OK', { duration: 2500 });
    } catch (e: any) {
      this.snackBar.open('Failed to approve: ' + (e.message || 'Try again'), 'Dismiss', { duration: 3000 });
    } finally {
      this.processingId = null;
    }
  }

  async deleteSkill(id: number) {
    this.processingId = id;
    try {
      await this.skillService.deleteSkill(id);
      await this.loadData();
      this.snackBar.open('Skill deleted.', 'OK', { duration: 2500 });
    } catch (e: any) {
      this.snackBar.open('Failed to delete skill', 'Dismiss', { duration: 3000 });
    } finally {
      this.processingId = null;
    }
  }

  async deleteUser(id: number) {
    this.processingId = id;
    try {
      await this.userService.deleteUser(id);
      await this.loadData();
      this.snackBar.open('User removed from database.', 'OK', { duration: 2500 });
    } catch (e: any) {
      this.snackBar.open('Failed to remove user', 'Dismiss', { duration: 3000 });
    } finally {
      this.processingId = null;
    }
  }

  async deleteGroup(id: number) {
    this.processingId = id;
    try {
      await this.groupService.deleteGroup(id);
      await this.loadData();
      this.snackBar.open('Group removed from database.', 'OK', { duration: 2500 });
    } catch (e: any) {
      this.snackBar.open('Failed to remove group', 'Dismiss', { duration: 3000 });
    } finally {
      this.processingId = null;
    }
  }

  async addSkill() {
    if (!this.skillForm.value.name) return;
    try {
      await this.skillService.createSkill({
        name: this.skillForm.value.name || '',
        category: this.skillForm.value.category || 'General'
      });
      this.skillForm.reset();
      await this.loadData();
      this.snackBar.open('Skill added!', 'OK', { duration: 2500 });
    } catch (e: any) {
      this.snackBar.open('Failed to add skill: ' + (e.message || 'Try again'), 'Dismiss', { duration: 3000 });
    }
  }

  isProcessing(id: number) { return this.processingId === id; }

  skillLabel(skill: string | number): string {
    if (typeof skill === 'string' && Number.isNaN(Number(skill))) {
      return skill;
    }
    const id = typeof skill === 'number' ? skill : Number(skill);
    return this.skills.find(item => item.id === id)?.name ?? String(skill);
  }
}
