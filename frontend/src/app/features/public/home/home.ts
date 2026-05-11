import { Component, OnInit, signal, ElementRef, QueryList, ViewChildren, AfterViewInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthStore } from '../../../core/store/auth.store';
import { inject } from '@angular/core';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home implements AfterViewInit {
  readonly store = inject(AuthStore);
  @ViewChildren('animateEl') animateEls!: QueryList<ElementRef>;

  stats = [
    { value: '10,000+', label: 'Learners', icon: '👩‍🎓' },
    { value: '500+',    label: 'Mentors',  icon: '🧑‍🏫' },
    { value: '25,000+', label: 'Sessions', icon: '📅' },
    { value: '95%',     label: 'Success Rate', icon: '🏆' },
  ];

  features = [
    {
      icon: '🎯',
      title: 'Smart Mentor Matching',
      desc: 'AI-powered matching connects you with the perfect mentor based on your goals, skills, and learning style.',
      color: 'blue'
    },
    {
      icon: '🎥',
      title: 'Live Interactive Sessions',
      desc: 'Real-time video sessions with screen sharing, whiteboards, and collaborative tools built in.',
      color: 'purple'
    },
    {
      icon: '📊',
      title: 'Skill Tracking Dashboard',
      desc: 'Visualize your growth with detailed analytics, progress charts, and milestone tracking.',
      color: 'teal'
    },
    {
      icon: '🌐',
      title: 'Learning Communities',
      desc: 'Join topic-based groups, share resources, and learn alongside peers on the same journey.',
      color: 'orange'
    },
  ];

  steps = [
    { num: '01', title: 'Create Your Profile', desc: 'Sign up and tell us about your goals, current skills, and what you want to learn.' },
    { num: '02', title: 'Explore Mentors', desc: 'Browse our curated network of verified experts across tech, design, business, and more.' },
    { num: '03', title: 'Book a Session', desc: 'Schedule 1-on-1 sessions at your convenience with instant calendar integration.' },
    { num: '04', title: 'Track Your Progress', desc: 'Monitor your skill growth, session history, and achievements on your personal dashboard.' },
  ];

  mentors = [
    { name: 'Priya Sharma', role: 'Senior Software Engineer', company: 'Google', expertise: ['React', 'Node.js', 'System Design'], rating: 4.9, sessions: 312, avatar: 'PS', color: '#2563eb' },
    { name: 'Arjun Mehta', role: 'Product Manager', company: 'Microsoft', expertise: ['Product Strategy', 'Agile', 'UX'], rating: 4.8, sessions: 245, avatar: 'AM', color: '#7c3aed' },
    { name: 'Kavya Reddy', role: 'Data Scientist', company: 'Amazon', expertise: ['ML', 'Python', 'Analytics'], rating: 5.0, sessions: 189, avatar: 'KR', color: '#0f766e' },
    { name: 'Rahul Gupta', role: 'UX Designer', company: 'Figma', expertise: ['UI/UX', 'Figma', 'Design Systems'], rating: 4.9, sessions: 278, avatar: 'RG', color: '#d97706' },
  ];

  testimonials = [
    { text: 'SkillSync helped me land my first tech job! My mentor guided me through every step of the interview process.', author: 'Sneha K.', role: 'Software Developer', avatar: 'SK' },
    { text: 'Mentoring made easy and impactful. I went from zero to deploying my first app in just 3 months.', author: 'Vikram P.', role: 'Full Stack Developer', avatar: 'VP' },
    { text: 'The skill tracking dashboard kept me accountable. I could literally see my progress every week.', author: 'Ananya M.', role: 'Data Analyst', avatar: 'AM' },
  ];

  ngAfterViewInit() {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
          }
        });
      },
      { threshold: 0.1, rootMargin: '0px 0px -50px 0px' }
    );

    this.animateEls.forEach(el => observer.observe(el.nativeElement));
  }
}
