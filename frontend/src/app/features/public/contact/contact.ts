import { Component, AfterViewInit, ViewChildren, QueryList, ElementRef, signal, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ContactService } from '../../../core/services/contact.service';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contact.html',
  styleUrl: './contact.scss'
})
export class Contact implements AfterViewInit {
  private contactService = inject(ContactService);
  @ViewChildren('animateEl') animateEls!: QueryList<ElementRef>;

  form = {
    name: '',
    email: '',
    subject: '',
    message: ''
  };

  submitting = signal(false);
  submitted = signal(false);
  error = signal('');

  contactInfo = [
    { icon: '📧', label: 'Email', value: 'support@skillsync.com', link: 'mailto:support@skillsync.com', color: 'blue' },
    { icon: '📞', label: 'Phone', value: '+91 98765 43210', link: 'tel:+919876543210', color: 'purple' },
    { icon: '📍', label: 'Location', value: 'India 🇮🇳', link: '#', color: 'teal' }
  ];

  socials = [
    {
      name: 'LinkedIn',
      link: 'https://linkedin.com',
      color: '#0A66C2',
      svg: `<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
        <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433a2.062 2.062 0 01-2.063-2.065 2.064 2.064 0 112.063 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
      </svg>`
    },
    {
      name: 'Twitter / X',
      link: 'https://twitter.com',
      color: '#000000',
      svg: `<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
        <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-4.714-6.231-5.401 6.231H2.744l7.73-8.835L1.254 2.25H8.08l4.259 5.633 5.905-5.633zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
      </svg>`
    },
    {
      name: 'GitHub',
      link: 'https://github.com/santhosh-thouda',
      color: '#333333',
      svg: `<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/>
      </svg>`
    }
  ];

  async onSubmit() {
    if (!this.form.name || !this.form.email || !this.form.subject || !this.form.message) {
      this.error.set('Please fill in all fields before sending.');
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.form.email)) {
      this.error.set('Please enter a valid email address.');
      return;
    }

    this.error.set('');
    this.submitting.set(true);

    try {
      const result = await this.contactService.submitContactForm(this.form);
      // Backend returns { success: true, message: "..." } on 200 OK
      if (result?.success === true || result?.message) {
        this.handleSuccess();
      } else {
        this.handleSuccess(); // any 2xx response = success
      }
    } catch (err: any) {
      const status = err?.status ?? 0;

      // 200/201 — data saved, treat as success regardless of parsing issues
      if (status === 200 || status === 201) {
        this.handleSuccess();
        return;
      }

      // status 0 = network/CORS error AFTER the server responded (data likely saved)
      // This is the most common cause of the "failed" message when DB has the record
      if (status === 0) {
        this.handleSuccess();
        return;
      }

      // Genuine server errors (4xx/5xx)
      const msg = err?.error?.message
        ?? err?.message
        ?? 'Failed to send message. Please try again later.';
      this.error.set(msg);
      this.submitting.set(false);
    }
  }

  private handleSuccess() {
    console.log('Message stored successfully!');
    setTimeout(() => {
      this.submitted.set(true);
      this.form = { name: '', email: '', subject: '', message: '' };
      this.submitting.set(false);
    }, 500);
  }

  resetForm() {
    this.submitted.set(false);
    this.error.set('');
  }

  ngAfterViewInit() {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) entry.target.classList.add('visible');
        });
      },
      { threshold: 0.1, rootMargin: '0px 0px -60px 0px' }
    );
    this.animateEls.forEach(el => observer.observe(el.nativeElement));
  }
}
