# Low-Level Design (LLD) - Frontend

## 1. Frontend Architecture & Core Paradigm

The SkillSync frontend is a modular Single Page Application (SPA) built using **Angular 21**. It focuses on a reactive, module-less architecture designed for scalability.
- **Standalone Components**: Eliminates `NgModules`, reducing boilerplate and improving build tree-shaking.
- **Signal-Based Reactivity**: Angular Signals (`@ngrx/signals`) drive synchronous, glitch-free state management and DOM updates without RxJS overhead.
- **Smart/Dumb Components**: Data fetching is isolated in Smart components, passing pure state down to Presentational (Dumb) components via `@Input()`.
- **Utility-First Styling**: Tailwind CSS is used globally for responsive UI components.

---

## 2. Structural Directory Hierarchy

The repository enforces feature-based lazy loading to optimize the initial Time-to-Interactive (TTI).

```text
src/app/
├── core/                        # Singleton services & configurations
│   ├── guards/                  # Route protection (AuthGuard, RoleGuard)
│   ├── interceptors/            # HTTP Middleware (JWT, Error Handling)
│   ├── services/                # API Clients (Auth, Chat, Mentors)
│   └── store/                   # Global State (auth.store.ts)
├── layout/                      # UI Structural Components
│   ├── shell/                   # Authenticated layout (Sidebar, Header)
│   └── public-layout/           # Unauthenticated landing layout
├── features/                    # Lazy-loaded business domains
│   ├── auth/                    # Login, Registration
│   ├── dashboard/               # Aggregated user metrics
│   ├── groups/                  # Study groups & WebSocket chat
│   ├── mentors/                 # Mentor discovery catalog
│   ├── sessions/                # Booking wizards & session management
│   └── profile/                 # Profile management forms
├── shared/                      # Reusable presentational components
│   ├── components/              # Buttons, Modals, Spinners
│   ├── directives/              # ClickOutside, ScrollToBottom
│   └── ui-models/               # Shared TypeScript interfaces
├── environments/                # Prod/Dev API configurations
└── app.routes.ts                # Main application routing
```

---

## 3. Global & Local State Management

State is managed entirely by `@ngrx/signals`, providing deeply reactive data stores that update the UI synchronously.

### 3.1 Global Context (`AuthStore`)
The `AuthStore` is a globally injected singleton that holds user identity and token state.
- **Implementation**: Uses `signalStore`, `withState`, and `withMethods`.
- **State Interface**:
  ```typescript
  export type AuthState = {
    user: UserProfile | null;
    token: string | null;
    isAuthenticated: boolean;
  }
  ```
- **Component Injection**:
  ```typescript
  export class HeaderComponent {
    readonly authStore = inject(AuthStore);
    // Computed signal updates automatically when user changes
    userName = computed(() => this.authStore.user()?.name || 'Guest');
  }
  ```

### 3.2 Local Feature State
Complex views maintain isolated state using local component Signals.
```typescript
export class GroupChatComponent {
  messages = signal<ChatMessage[]>([]);
  isTyping = signal<boolean>(false);
  
  // Dynamically computes unread count without manual recalculation loops
  unreadCount = computed(() => this.messages().filter(m => !m.isRead).length);
}
```

---

## 4. Routing & Access Control

### 4.1 Route Configuration
The Router uses `loadComponent` to lazy-load feature modules.
```typescript
export const routes: Routes = [
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      { path: '', loadComponent: () => import('./features/public/home/home.component') },
      { path: 'login', loadComponent: () => import('./features/auth/login/login.component') }
    ]
  },
  {
    path: 'app',
    component: ShellComponent,
    canActivate: [AuthGuard], // Secures the internal app
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component') },
      { path: 'mentors', loadComponent: () => import('./features/mentors/mentor-list.component') },
      { path: 'groups/:id', loadComponent: () => import('./features/groups/group-chat.component') }
    ]
  }
];
```

### 4.2 Route Guards
- **`AuthGuard`**: Checks `authStore.isAuthenticated()`. If false, forces a redirect to `/login`.
- **`RoleGuard`**: Ensures the user possesses required privileges (e.g., `'ADMIN'`) before loading sensitive routes.

---

## 5. Network Layer & API Clients

### 5.1 HTTP Interceptors
- **`JwtInterceptor`**: Clones outbound HTTP requests to append the `Authorization: Bearer <token>` header, ensuring secure backend communication.
- **`ErrorInterceptor`**: Catches `401 Unauthorized` responses to clear local storage and force a logout. Dispatches Toast notifications for `400` validation errors.

### 5.2 Real-Time Communication (`chat.service.ts`)
Facilitates STOMP protocol over WebSockets for live group chat.
```typescript
@Injectable({ providedIn: 'root' })
export class ChatService {
  private stompClient: Client;
  public incomingMessage = signal<ChatMessage | null>(null);

  public connect(token: string): void {
    this.stompClient = new Client({
      brokerURL: 'ws://api-gateway:8080/ws/chat',
      connectHeaders: { Authorization: `Bearer ${token}` }
    });
    this.stompClient.activate();
  }

  public subscribe(groupId: string): void {
    this.stompClient.subscribe(`/topic/group.${groupId}`, (message) => {
       this.incomingMessage.set(JSON.parse(message.body));
    });
  }

  public send(groupId: string, content: string): void {
    this.stompClient.publish({
      destination: `/app/chat.sendMessage`,
      body: JSON.stringify({ groupId, content })
    });
  }
}
```

---

## 6. Critical Component Designs

### 6.1 `SessionBookComponent` (Booking Engine)
- **Design**: Implements a multi-step stepper UI.
- **Validation**: Relies on Angular Reactive Forms (`FormGroup`) with asynchronous validators that ping the backend to confirm a mentor's time slot is still open.
- **State Handling**: Upon submitting, a boolean signal `isSubmitting.set(true)` disables all interactable elements to prevent duplicate API requests.

### 6.2 `MentorListComponent` (Catalog Grid)
- **Data Fetching**: Utilizes `valueChanges` on a reactive search bar with a `debounceTime(300)` operator to prevent API spam while typing.
- **Performance**: Implements the Angular CDK `cdk-virtual-scroll-viewport` to render only the visible subset of DOM nodes, ensuring smooth 60fps scrolling even with thousands of mentors.

### 6.3 `GroupChatComponent` (Live Messaging)
- **Scroll Hook**: Employs a custom `@ViewChild` directive that auto-scrolls to the bottom of the chat container whenever the `messages()` signal updates, provided the user hasn't manually scrolled up.
- **Update Cycle**: Uses a Signal `effect()` to listen to `ChatService.incomingMessage`. Appends new payloads directly into the array to avoid triggering deep component tree checks.

---

## 7. Performance & Optimization

- **Change Detection**: Strict adherence to `ChangeDetectionStrategy.OnPush`. Angular only re-renders views when input signals mutate, bypassing standard Zone.js dirty-checking.
- **Asset Loading**: Avatar images use Angular's `NgOptimizedImage` (`ngSrc`) directive. This enforces explicit width/height ratios to prevent Cumulative Layout Shift (CLS) and defers loading of off-screen images.
- **Bundle Strategy**: Granular deep imports from libraries (`import { map } from 'rxjs/operators'`) ensure webpack tree-shaking discards unused code.

---

## 8. Security Mitigations

- **XSS Prevention**: Angular's strict `DomSanitizer` automatically sanitizes all user-generated content (e.g., chat messages, mentor bios) before it enters the DOM.
- **Token Handling**: The application avoids exposing JWTs in generic URL parameters, favoring strictly HTTP headers via the `JwtInterceptor`.
