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
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { GroupService, Group } from '../../../core/services/group.service';
import { AuthStore } from '../../../core/store/auth.store';
import { GroupChat } from '../group-chat/group-chat';

@Component({
  selector: 'app-group-list',
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
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    GroupChat
  ],
  templateUrl: './group-list.html',
  styleUrl: './group-list.scss',
})
export class GroupList implements OnInit {
  private fb = new FormBuilder();
  private groupService = inject(GroupService);
  readonly authStore = inject(AuthStore);
  private snackBar = inject(MatSnackBar);

  readonly filterForm = this.fb.group({ search: [''] });
  readonly createForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.minLength(8)]]
  });

  groups: Group[] = [];
  filteredGroups: Group[] = [];
  showCreateForm = false;
  isCreating = false;
  isLoading = true;
  activeGroupId: number | null = null;

  get activeGroup(): Group | null {
    return this.groups.find(g => g.id === this.activeGroupId) ?? null;
  }

  get joinedCount(): number {
    return this.groups.filter(g => this.isJoined(g)).length;
  }

  getGroupGradient(id: number): string {
    const gradients = [
      'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
      'linear-gradient(135deg, #3b82f6 0%, #2dd4bf 100%)',
      'linear-gradient(135deg, #f97316 0%, #ef4444 100%)',
      'linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%)'
    ];
    return gradients[id % gradients.length];
  }

  async ngOnInit() {
    await this.loadGroups();
    this.filterForm.valueChanges.subscribe(() => this.filterGroups());
  }

  async loadGroups() {
    this.isLoading = true;
    try {
      this.groups = await this.groupService.getAllGroups();
      this.filterGroups();
    } catch (e) {
      this.snackBar.open('Failed to load groups', 'Dismiss', { duration: 3000 });
    } finally {
      this.isLoading = false;
    }
  }

  filterGroups() {
    const search = (this.filterForm.value.search ?? '').trim().toLowerCase();
    this.filteredGroups = search
      ? this.groups.filter(g => [g.name, g.description].join(' ').toLowerCase().includes(search))
      : [...this.groups];
  }

  isJoined(group: Group): boolean {
    const userId = this.authStore.user()?.id;
    return userId ? (group.members?.includes(userId) ?? false) : false;
  }

  openChat(group: Group) {
    if (!this.isJoined(group)) {
      this.snackBar.open('Join the group first to access the chat.', 'OK', { duration: 2500 });
      return;
    }
    this.activeGroupId = this.activeGroupId === group.id ? null : group.id;
  }

  async toggleJoin(group: Group) {
    const userId = this.authStore.user()?.id;
    if (!userId) {
      this.snackBar.open('Please login again before joining groups.', 'Dismiss', { duration: 3000 });
      return;
    }
    try {
      if (this.isJoined(group)) {
        if (this.activeGroupId === group.id) this.activeGroupId = null;
        await this.groupService.leaveGroup(group.id, userId);
        this.snackBar.open(`Left '${group.name}'`, 'OK', { duration: 2500 });
      } else {
        await this.groupService.joinGroup(group.id, userId);
        this.snackBar.open(`Joined '${group.name}'!`, 'OK', { duration: 2500 });
      }
      await this.loadGroups();
    } catch (e: any) {
      this.snackBar.open('Operation failed: ' + (e.message || 'Try again'), 'Dismiss', { duration: 3000 });
    }
  }

  async createGroup() {
    if (this.createForm.invalid) { this.createForm.markAllAsTouched(); return; }
    this.isCreating = true;
    try {
      const { name, description } = this.createForm.value;
      await this.groupService.createGroup({
        name: name ?? '',
        description: description ?? '',
        createdBy: this.authStore.user()?.id ?? 0
      });
      this.createForm.reset();
      this.showCreateForm = false;
      await this.loadGroups();
      this.snackBar.open('Group created successfully!', 'OK', { duration: 3000 });
    } catch (e: any) {
      this.snackBar.open('Failed to create group: ' + (e.message || 'Try again'), 'Dismiss', { duration: 4000 });
    } finally {
      this.isCreating = false;
    }
  }
}
