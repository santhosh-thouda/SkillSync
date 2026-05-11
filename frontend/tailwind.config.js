/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts,scss}"
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'Plus Jakarta Sans', 'sans-serif'],
        display: ['Outfit', 'Inter', 'sans-serif'],
      },
      colors: {
        primary: {
          DEFAULT: '#2563EB',
          hover: '#1D4ED8',
          50: '#EFF6FF',
          100: '#DBEAFE',
          200: '#BFDBFE',
          500: '#2563EB',
          600: '#1D4ED8',
          900: '#1E3A8A',
        },
        secondary: {
          DEFAULT: '#14B8A6',
          50: '#F0FDFA',
          100: '#CCFBF1',
          200: '#99F6E4',
          500: '#14B8A6',
          600: '#0D9488',
        },
        accent: '#7C3AED',
        surface: {
          DEFAULT: '#FFFFFF',
          muted: '#F8FAFC',
          subtle: '#F1F5F9',
          border: '#E2E8F0',
          hover: '#F8FAFC',
        },
        ink: '#0F172A',
        muted: '#64748B',
        success: '#10B981',
        warning: '#F59E0B',
        danger: '#EF4444',
      },
      backgroundImage: {
        'primary-gradient': 'linear-gradient(135deg, #2563EB, #7C3AED)',
        'secondary-gradient': 'linear-gradient(135deg, #14B8A6, #2563EB)',
        'soft-mesh': 'radial-gradient(circle at 12% 18%, rgba(37,99,235,0.16), transparent 28%), radial-gradient(circle at 84% 12%, rgba(20,184,166,0.14), transparent 28%), linear-gradient(180deg, #F8FAFC 0%, #EEF2FF 100%)',
      },
      boxShadow: {
        'soft': '0 18px 50px rgba(15, 23, 42, 0.08)',
        'soft-lg': '0 24px 70px rgba(15, 23, 42, 0.13)',
        'ring-soft': '0 0 0 1px rgba(37,99,235,0.08), 0 20px 55px rgba(15,23,42,0.1)',
        'glow': '0 16px 40px rgba(37, 99, 235, 0.22)',
      },
      animation: {
        'fade-in': 'fadeIn 0.6s cubic-bezier(0.16,1,0.3,1) forwards',
        'slide-up': 'slideUp 0.7s cubic-bezier(0.16,1,0.3,1) forwards',
        'scale-in': 'scaleIn 0.35s cubic-bezier(0.16,1,0.3,1) forwards',
        'shimmer': 'shimmer 1.4s linear infinite',
        'spin-slow': 'spin 3s linear infinite',
      },
      keyframes: {
        fadeIn: {
          from: { opacity: '0' },
          to: { opacity: '1' },
        },
        slideUp: {
          from: { opacity: '0', transform: 'translateY(18px) scale(0.985)' },
          to: { opacity: '1', transform: 'translateY(0) scale(1)' },
        },
        scaleIn: {
          from: { opacity: '0', transform: 'scale(0.97)' },
          to: { opacity: '1', transform: 'scale(1)' },
        },
        shimmer: {
          from: { backgroundPosition: '-200% 0' },
          to: { backgroundPosition: '200% 0' },
        },
      },
      borderRadius: {
        '2xl': '0.875rem',
        '3xl': '1.25rem',
      },
    },
  },
  plugins: [require('@tailwindcss/forms')],
}
