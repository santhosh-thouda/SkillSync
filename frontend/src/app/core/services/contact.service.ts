import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { firstValueFrom } from 'rxjs';

export interface ContactMessage {
  name: string;
  email: string;
  subject: string;
  message: string;
  submittedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private api = inject(ApiService);

  submitContactForm(message: ContactMessage): Promise<any> {
    // defaultValue ensures firstValueFrom never throws EmptyError
    // if the server returns 200 with no body or the observable completes early
    return firstValueFrom(
      this.api.post<any>('/groups/contact', message),
      { defaultValue: { success: true } }
    );
  }
}
