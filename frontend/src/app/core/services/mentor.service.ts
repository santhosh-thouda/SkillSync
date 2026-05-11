import { Injectable, inject } from '@angular/core';
import { firstValueFrom, timeout, throwError } from 'rxjs';
import { ApiService } from './api.service';

export interface Mentor {
  id: number;
  userId?: number;
  name?: string;
  earnings?: number;
  bio: string;
  experience: number;
  hourlyRate: number;
  rating?: number;
  skills: Array<string | number>;
  available?: boolean;
  approved?: boolean;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MentorService {
  private api = inject(ApiService);

  async getAllMentors(): Promise<Mentor[]> {
    return this.api.get<Mentor[]>('/mentors').toPromise().then(res => res || []);
  }

  async getMentorById(id: number): Promise<Mentor> {
    const res = await this.api.get<Mentor>(`/mentors/${id}`).toPromise();
    if (!res) throw new Error('Mentor not found');
    return res;
  }

  async getMentorByUserId(userId: number): Promise<Mentor | null> {
    try {
      const res = await firstValueFrom(
        this.api.get<Mentor>(`/mentors/user/${userId}`).pipe(
          timeout({ each: 5000, with: () => throwError(() => new Error('Mentor profile lookup timed out.')) })
        )
      );
      return res || null;
    } catch (error: any) {
      if (error?.status === 404) return null;
      throw error;
    }
  }

  async searchMentors(filters: any): Promise<Mentor[]> {
    // Remove empty values to keep query params clean
    const cleanFilters: any = {};
    Object.keys(filters).forEach(key => {
      if (filters[key] !== null && filters[key] !== undefined && filters[key] !== '') {
        cleanFilters[key] = filters[key];
      }
    });
    const params = new URLSearchParams(cleanFilters).toString();
    const url = params ? `/mentors?${params}` : '/mentors';
    return this.api.get<Mentor[]>(url).toPromise().then(res => res || []);
  }

  async apply(data: any): Promise<Mentor> {
    const res = await this.api.post<Mentor>('/mentors/apply', data).toPromise();
    if (!res) throw new Error('Application failed');
    return res;
  }

  async updateAvailability(id: number, available: boolean): Promise<Mentor> {
    const res = await this.api.put<Mentor>(`/mentors/${id}/availability`, { available }).toPromise();
    if (!res) throw new Error('Failed to update availability');
    return res;
  }

  async updateMentor(id: number, data: Partial<Mentor>): Promise<Mentor> {
    const res = await this.api.put<Mentor>(`/mentors/${id}`, data).toPromise();
    if (!res) throw new Error('Failed to update mentor profile');
    return res;
  }

  async approveMentor(id: number): Promise<any> {
    return this.api.put<any>(`/mentors/${id}/approve`, {}).toPromise();
  }
}
