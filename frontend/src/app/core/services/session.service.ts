import { Injectable, inject } from '@angular/core';
import { firstValueFrom, timeout, throwError } from 'rxjs';
import { ApiService } from './api.service';

export interface Session {
  id: number;
  mentorId: number;
  learnerId: number;
  mentorName?: string;
  learnerName?: string;
  sessionDate: string;
  status: 'REQUESTED' | 'ACCEPTED' | 'REJECTED' | 'COMPLETED' | 'CANCELLED';
  topic?: string;
}

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private api = inject(ApiService);

  async getUserSessions(userId: number): Promise<Session[]> {
    const res = await firstValueFrom(
      this.api.get<Session[]>(`/sessions/user/${userId}`).pipe(
        timeout({ each: 5000, with: () => throwError(() => new Error('Session lookup timed out.')) })
      )
    );
    return res || [];
  }

  async getMentorSessions(mentorId: number): Promise<Session[]> {
    const res = await firstValueFrom(
      this.api.get<Session[]>(`/sessions/mentor/${mentorId}`).pipe(
        timeout({ each: 5000, with: () => throwError(() => new Error('Session lookup timed out.')) })
      )
    );
    return res || [];
  }

  async requestSession(data: { mentorId: number; mentorName: string; learnerId: number; learnerName: string; sessionDate: string; topic: string; hourlyRate: number }): Promise<Session> {
    const res = await firstValueFrom(
      this.api.post<Session>('/sessions', {
        mentorId: data.mentorId,
        learnerId: data.learnerId,
        mentorName: data.mentorName,
        learnerName: data.learnerName,
        sessionDate: data.sessionDate,
        hourlyRate: data.hourlyRate
      })
    );
    if (!res) throw new Error('Failed to request session');
    return { ...res, topic: data.topic };
  }

  async acceptSession(id: number): Promise<Session> {
    const res = await firstValueFrom(this.api.put<Session>(`/sessions/${id}/accept`, {}));
    if (!res) throw new Error('Failed to accept session');
    return res;
  }

  async rejectSession(id: number): Promise<Session> {
    const res = await firstValueFrom(this.api.put<Session>(`/sessions/${id}/reject`, {}));
    if (!res) throw new Error('Failed to reject session');
    return res;
  }

  async cancelSession(id: number): Promise<Session> {
    const res = await firstValueFrom(this.api.put<Session>(`/sessions/${id}/cancel`, {}));
    if (!res) throw new Error('Failed to cancel session');
    return res;
  }

  async completeSession(id: number): Promise<Session> {
    const res = await firstValueFrom(this.api.put<Session>(`/sessions/${id}/complete`, {}));
    if (!res) throw new Error('Failed to complete session');
    return res;
  }

  async updateSessionStatus(id: number, status: 'ACCEPTED' | 'REJECTED' | 'COMPLETED' | 'CANCELLED'): Promise<Session> {
    if (status === 'ACCEPTED') {
      return this.acceptSession(id);
    }
    if (status === 'REJECTED') {
      return this.rejectSession(id);
    }
    if (status === 'COMPLETED') {
      return this.completeSession(id);
    }
    return this.cancelSession(id);
  }
}
