import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

export interface User {
  id: number;
  email: string;
  name: string;
  roles: string[];
  profileImage?: string;
}

export interface AuthResponse {
  token: string;
  userId?: number;
  role?: string;
  user?: User; // Depending on what backend returns
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private api = inject(ApiService);

  async login(credentials: any): Promise<{token: string, user: User}> {
    const res = await this.api.post<any>('/auth/login', credentials).toPromise();
    if (!res) throw new Error('Authenticaton failed: No response from server.');
    
    // Assuming backend returns { token, userId, role }
    const token = res.token || res.jwt || res; 
    const resolvedToken = typeof token === 'string' ? token : token.token;
    
    if (!resolvedToken) throw new Error('Invalid response: Token missing.');

    // Attempt to decode user info from token
    const user = this.decodeToken(resolvedToken);
    
    // Override if backend returned explicit data
    if (res.userId) user.id = res.userId;
    if (res.role) {
      const normalizedRole = res.role.startsWith('ROLE_') ? res.role : `ROLE_${res.role}`;
      user.roles = [normalizedRole];
    }

    return { token: resolvedToken, user };
  }

  async register(data: any): Promise<any> {
    return await this.api.post<any>('/auth/register', data).toPromise();
  }

  async sendOtp(email: string, name: string): Promise<void> {
    await this.api.post<any>('/auth/send-otp', { email, name }).toPromise();
  }

  async verifyOtp(email: string, otp: string): Promise<void> {
    const res = await this.api.post<any>('/auth/verify-otp', { email, otp }).toPromise();
    if (!res) throw new Error('Verification failed');
  }

  private decodeToken(token: string): User {
    try {
      const payload = token.split('.')[1];
      const decodedInfo = JSON.parse(atob(payload));
      
      const rawRoles: string | string[] = decodedInfo.roles || decodedInfo.role || 'ROLE_LEARNER';
      const roleArray = Array.isArray(rawRoles) ? rawRoles : [rawRoles];
      const normalizedRoles = roleArray.map(r => r.startsWith('ROLE_') ? r : `ROLE_${r}`);

      return {
        id: decodedInfo.id || decodedInfo.userId || 0,
        email: decodedInfo.sub || decodedInfo.email || '',
        name: decodedInfo.name || decodedInfo.sub?.split('@')[0] || 'User',
        roles: normalizedRoles
      };
    } catch (e) {
      console.error('JWT Decode failed', e);
      throw new Error('Invalid authentication token received from server.');
    }
  }
}
