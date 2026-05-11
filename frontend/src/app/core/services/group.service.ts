import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

export interface Group {
  id: number;
  name: string;
  description: string;
  createdBy: number;
  members: number[];
}

@Injectable({
  providedIn: 'root'
})
export class GroupService {
  private api = inject(ApiService);

  async getAllGroups(): Promise<Group[]> {
    const res = await this.api.get<Group[]>('/groups').toPromise();
    return res || [];
  }

  async createGroup(data: { name: string; description: string; createdBy: number }): Promise<Group> {
    const res = await this.api.post<Group>('/groups', data).toPromise();
    if (!res) throw new Error('Failed to create group');
    return res;
  }

  async joinGroup(id: number, userId: number): Promise<Group> {
    const res = await this.api.post<Group>(`/groups/${id}/join?userId=${userId}`, {}).toPromise();
    if (!res) throw new Error('Failed to join group');
    return res;
  }

  async leaveGroup(id: number, userId: number): Promise<Group> {
    const res = await this.api.post<Group>(`/groups/${id}/leave?userId=${userId}`, {}).toPromise();
    if (!res) throw new Error('Failed to leave group');
    return res;
  }

  async deleteGroup(id: number): Promise<void> {
    await this.api.delete<any>(`/groups/${id}`).toPromise();
  }
}
