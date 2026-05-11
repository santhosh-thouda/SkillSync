import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from './api.service';

export interface UserProfile {
  id: number;
  email: string;
  name: string;
  profileImage?: string;
  role: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private api = inject(ApiService);

  async getUser(id: number): Promise<UserProfile> {
    const res = await firstValueFrom(this.api.get<UserProfile>(`/users/${id}`));
    if (!res) throw new Error('User not found');
    return res;
  }

  async getUserByEmail(email: string): Promise<UserProfile> {
    const res = await firstValueFrom(this.api.get<UserProfile>(`/users/email/${encodeURIComponent(email)}`));
    if (!res) throw new Error('User not found');
    return res;
  }

  async getAllUsers(): Promise<UserProfile[]> {
    const res = await firstValueFrom(this.api.get<UserProfile[]>('/users'));
    return res || [];
  }

  async updateUser(id: number, data: { name?: string; bio?: string; profileImage?: string }): Promise<UserProfile> {
    const res = await firstValueFrom(this.api.put<UserProfile>(`/users/${id}`, {
      name: data.name,
      profileImage: data.profileImage
    }));
    if (!res) throw new Error('Failed to update user');
    return res;
  }

  async deleteUser(id: number): Promise<void> {
    await firstValueFrom(this.api.delete<any>(`/users/${id}`));
  }

  async createUser(data: { name: string; email: string; role: string; profileImage?: string }): Promise<UserProfile> {
    const res = await firstValueFrom(this.api.post<UserProfile>('/users', data));
    if (!res) throw new Error('Failed to create user profile');
    return res;
  }
}
