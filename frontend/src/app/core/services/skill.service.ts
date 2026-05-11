import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

export interface Skill {
  id: number;
  name: string;
  category?: string;
}

@Injectable({
  providedIn: 'root'
})
export class SkillService {
  private api = inject(ApiService);
  private skillsCache: Skill[] | null = null;

  async getAllSkills(): Promise<Skill[]> {
    if (this.skillsCache) return this.skillsCache;
    
    try {
      const res = await this.api.get<Skill[]>('/skills').toPromise();
      this.skillsCache = res || [];
      return this.skillsCache;
    } catch (e) {
      console.warn('Failed to fetch skills, returning empty list');
      return [];
    }
  }

  async createSkill(data: { name: string; category?: string }): Promise<Skill> {
    const res = await this.api.post<Skill>('/skills', data).toPromise();
    if (!res) throw new Error('Failed to create skill');
    this.skillsCache = null; // Invalidate cache
    return res;
  }

  async deleteSkill(id: number): Promise<void> {
    await this.api.delete<any>(`/skills/${id}`).toPromise();
    this.skillsCache = null; // Invalidate cache
  }
}
