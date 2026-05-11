import { Injectable, inject } from '@angular/core';
import { firstValueFrom, timeout, throwError } from 'rxjs';
import { ApiService } from './api.service';

export interface Review {
  id: number;
  mentorId: number;
  userId: number;
  userName?: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface ReviewRequest {
  mentorId: number;
  userId: number;
  userName?: string;
  rating: number;
  comment: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private api = inject(ApiService);

  async getReviewsByMentor(mentorId: number): Promise<Review[]> {
    try {
      const response = await firstValueFrom(
        this.api.get<Review[]>(`/reviews/mentor/${mentorId}`).pipe(
          timeout({
            each: 8000,
            with: () => throwError(() => new Error('Review retrieval timed out.'))
          })
        )
      );
      return response ?? [];
    } catch (error) {
      console.error('Review fetch error:', error);
      return [];
    }
  }

  async addReview(request: ReviewRequest): Promise<Review> {
    const response = await firstValueFrom(this.api.post<Review>('/reviews', request));
    if (!response) {
      throw new Error('Empty review response');
    }
    return response;
  }
}
