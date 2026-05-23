# SkillSync Frontend Architecture & Concepts

This document outlines the core concepts, libraries, methodologies, and structural patterns utilized in the SkillSync Frontend application.

## 1. Core Framework & Stack
*   **Angular 21**: The application is built on the latest Angular version, leveraging modern Angular features.
*   **Standalone Components**: Angular components are designed as standalone, eliminating the need for `NgModules` and streamlining dependency injection.
*   **TypeScript**: Used for strict static typing across the entire frontend application (`~5.9.2`).

## 2. State Management & Reactivity
*   **Angular Signals**: Used extensively for fine-grained reactivity in component templates, replacing traditional `Zone.js` change detection overhead where possible.
*   **NgRx SignalStore (`@ngrx/signals`)**: A lightweight, reactive state management solution built on top of Angular Signals. It manages global and feature-level state without the boilerplate of classic NgRx Store.
*   **RxJS**: Used for handling asynchronous data streams, particularly for HTTP requests (`HttpClient`) and WebSocket event streams.

## 3. Styling & UI Components
*   **Tailwind CSS (v3.4)**: The primary styling engine. A utility-first CSS framework that allows rapid UI development directly in HTML templates.
    *   **Plugin**: `@tailwindcss/forms` is used to provide base styling for form elements.
    *   **PostCSS & Autoprefixer**: Used for CSS processing and cross-browser vendor prefixing.
*   **Angular Material & CDK (v21)**: Provides pre-built, accessible, and high-quality UI components (like dialogs, snackbars, and data tables) and component dev kits (CDK) for building custom behaviors.

## 4. Real-time Communication
*   **WebSockets (STOMP)**: Real-time features, such as the Group Chat, are powered by WebSockets.
*   **Libraries**:
    *   `@stomp/stompjs`: A STOMP client for connecting to the Spring Boot WebSocket broker.
    *   `sockjs-client`: Provides a WebSocket-like object with a fallback mechanism for older browsers or restrictive networks.

## 5. Architectural Structure (Feature-Sliced Design)
The `src/app` directory follows a highly scalable, feature-sliced architecture:
*   **`core/`**: Contains singleton services, global state, and configuration that are instantiated once.
    *   **`guards/`**: Route guards (e.g., `auth.guard.ts`) to protect routes from unauthorized access.
    *   **`interceptors/`**: HTTP Interceptors (e.g., for attaching JWT bearer tokens to requests and handling global errors).
    *   **`services/`**: Global API client services and utility services.
    *   **`store/`**: Global NgRx Signal stores.
*   **`features/`**: Contains feature-specific modules/components, mirroring the microservices backend.
    *   `admin`, `auth`, `dashboard`, `groups`, `mentors`, `profile`, `public`, `reviews`, `sessions`.
*   **`layout/`**: Contains shell layouts (e.g., `public-layout` for unauthenticated pages, `shell` with sidebars/navbars for authenticated users).
*   **`shared/`**: Contains dumb/presentational components, pipes, and directives that are shared across multiple features.

## 6. Routing
*   **Angular Router**: Manages lazy loading of feature components and structural routing.
*   **Guards**: Uses modern functional route guards (e.g., `canActivate`) to enforce security policies at the frontend layer.

## 7. Testing
*   **Vitest**: A blazing fast, Vite-native testing framework used instead of the traditional Karma/Jasmine setup for unit testing.
*   **JSDOM**: Simulates a browser environment for Vitest to render and test Angular components.

## 8. Build & Deployment
*   **Angular CLI / Build (`@angular/build`)**: Uses the new esbuild-based Angular builder for faster compilation times.
*   **Docker**: The frontend is containerized using a `Dockerfile`.
*   **Nginx**: Used as the web server inside the Docker container to serve the compiled static Angular files (`dist/`) and handle client-side routing fallback (`try_files $uri $uri/ /index.html`).
