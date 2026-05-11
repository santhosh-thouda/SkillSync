import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

// Step 1: Fill details → Step 2: Enter OTP → Step 3: Done (redirect)
type Step = 'form' | 'otp' | 'done';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatSelectModule, MatProgressSpinnerModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  step: Step = 'form';
  hidePassword = true;
  isLoading = false;
  errorMessage = '';
  resendCooldown = 0;
  private resendTimer: any;

  registerForm: FormGroup = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['ROLE_LEARNER', Validators.required]
  });

  otpForm: FormGroup = this.fb.group({
    otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6), Validators.pattern(/^\d{6}$/)]]
  });

  get email() { return this.registerForm.get('email')?.value ?? ''; }
  get name() { return this.registerForm.get('name')?.value ?? ''; }

  // Step 1 → send OTP
  async onSubmit() {
    if (this.registerForm.invalid) { this.registerForm.markAllAsTouched(); return; }
    this.isLoading = true;
    this.errorMessage = '';
    try {
      await this.authService.sendOtp(this.email, this.name);
      this.step = 'otp';
      this.startResendCooldown();
    } catch (err: any) {
      this.errorMessage = err.error?.message || err.message || 'Failed to send verification code. Please try again.';
    } finally {
      this.isLoading = false;
    }
  }

  // Step 2 → verify OTP then register
  async onVerifyOtp() {
    if (this.otpForm.invalid) { this.otpForm.markAllAsTouched(); return; }
    this.isLoading = true;
    this.errorMessage = '';
    try {
      await this.authService.verifyOtp(this.email, this.otpForm.value.otp);
      // OTP verified — now register
      await this.authService.register(this.registerForm.value);
      this.step = 'done';
      setTimeout(() => this.router.navigate(['/auth/login']), 2000);
    } catch (err: any) {
      this.errorMessage = err.error?.message || err.message || 'Invalid or expired code. Please try again.';
    } finally {
      this.isLoading = false;
    }
  }

  async resendOtp() {
    if (this.resendCooldown > 0) return;
    this.isLoading = true;
    this.errorMessage = '';
    try {
      await this.authService.sendOtp(this.email, this.name);
      this.startResendCooldown();
    } catch (err: any) {
      this.errorMessage = err.error?.message || 'Failed to resend code.';
    } finally {
      this.isLoading = false;
    }
  }

  goBack() {
    this.step = 'form';
    this.otpForm.reset();
    this.errorMessage = '';
    clearInterval(this.resendTimer);
    this.resendCooldown = 0;
  }

  private startResendCooldown(seconds = 60) {
    this.resendCooldown = seconds;
    clearInterval(this.resendTimer);
    this.resendTimer = setInterval(() => {
      this.resendCooldown--;
      if (this.resendCooldown <= 0) clearInterval(this.resendTimer);
    }, 1000);
  }
}
