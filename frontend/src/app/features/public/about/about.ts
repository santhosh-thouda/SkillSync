import { Component, AfterViewInit, ViewChildren, QueryList, ElementRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { inject } from '@angular/core';
import { AuthStore } from '../../../core/store/auth.store';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './about.html',
  styleUrl: './about.scss'
})
export class About implements AfterViewInit {
  readonly store = inject(AuthStore);
  @ViewChildren('animateEl') animateEls!: QueryList<ElementRef>;

  mission = {
    icon: '🎯',
    title: 'Our Mission',
    text: 'Empower every learner through structured mentorship, personalized sessions, and a community that celebrates growth. We believe talent is evenly distributed — opportunity is not. SkillSync bridges that gap.'
  };

  vision = {
    icon: '🌍',
    title: 'Our Vision',
    text: 'To become the world\'s most trusted mentorship-driven learning platform — where every skill gap is closed, every career goal is achievable, and every mentor leaves a lasting impact.'
  };

  platformFeatures = [
    {
      icon: '🤝',
      title: 'Mentorship System',
      desc: 'Connect learners with verified industry experts through a smart matching system that aligns goals, skills, and availability.',
      color: 'blue'
    },
    {
      icon: '📅',
      title: 'Session Booking',
      desc: 'Seamless 1-on-1 session scheduling with calendar sync, reminders, and video integration built right in.',
      color: 'purple'
    },
    {
      icon: '📊',
      title: 'Skill Tracking',
      desc: 'Visual dashboards that track your learning milestones, session history, skill growth, and achievements over time.',
      color: 'teal'
    },
    {
      icon: '🌐',
      title: 'Learning Groups',
      desc: 'Topic-based community groups where learners collaborate, share resources, and grow together on shared journeys.',
      color: 'orange'
    }
  ];

  values = [
    { icon: '💡', title: 'Innovation', desc: 'We build smart tools that remove friction from learning.' },
    { icon: '🤗', title: 'Empathy', desc: 'Every feature is designed with the learner\'s journey in mind.' },
    { icon: '🔒', title: 'Trust', desc: 'All mentors are verified professionals. All sessions are secure.' },
    { icon: '🚀', title: 'Growth', desc: 'We measure success by the careers and lives we help transform.' }
  ];

  stats = [
    { value: '10,000+', label: 'Learners', icon: '👩‍🎓' },
    { value: '500+', label: 'Mentors', icon: '🧑‍🏫' },
    { value: '25,000+', label: 'Sessions', icon: '📅' },
    { value: '95%', label: 'Success Rate', icon: '🏆' }
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
      { threshold: 0.1, rootMargin: '0px 0px -60px 0px' }
    );
    this.animateEls.forEach(el => observer.observe(el.nativeElement));
  }
}
