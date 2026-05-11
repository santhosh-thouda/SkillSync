# Low-Level Design (LLD) - Backend

## 1. Architectural Paradigms

The SkillSync backend is a distributed system of Spring Boot 3.x microservices. It is designed around these core tenets:
- **Bounded Contexts**: Microservices are strictly isolated. The `group-service` cannot directly read the `user-service` database. Data fetching happens via Feign Clients or event logs.
- **Stateless Operation**: Application state is never stored in server memory. User sessions are verified on-the-fly using cryptographically signed JSON Web Tokens (JWTs).
- **Asynchronous Processing**: Heavy, non-blocking tasks (like emailing) are deferred to RabbitMQ message queues to ensure synchronous API responses remain under 100ms.
- **Fail-Fast & Resilience**: Built-in circuit breakers prevent cascading failures when downstream services (e.g., `notification-service`) become temporarily unavailable.
- **Domain-Driven Design (DDD)**: Each service revolves around a core aggregate root. For instance, the `Session` entity in `session-service` is the aggregate root, and all related updates occur through it.

---

## 2. Gateway & API Contracts

All external HTTP and WebSocket traffic enters through the Spring Cloud API Gateway running on port `8080`. The Gateway handles cross-cutting concerns like CORS and Rate Limiting before routing traffic.

### 2.1 Core API Endpoints

| Target Service | Endpoint Path | Method | Primary Payload | Response DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | `/api/v1/auth/login` | `POST` | `LoginRequest(email, password)` | `AuthResponse(jwt)` |
| **Auth** | `/api/v1/auth/register`| `POST` | `RegisterRequest(details)` | `201 Created` |
| **Auth** | `/api/v1/auth/otp/send`| `POST` | `SendOtpRequest(email)` | `200 OK` |
| **Auth** | `/api/v1/auth/otp/verify`| `POST`| `VerifyOtpRequest(email, otp)`| `200 OK` |
| **User** | `/api/v1/users/me` | `GET` | - | `UserDto` |
| **User** | `/api/v1/users/{id}` | `PUT` | `UserUpdateRequest(bio, ...)`| `UserDto` |
| **Mentor** | `/api/v1/mentors` | `GET` | `?skills=Java&sort=rating` | `Page<MentorDto>` |
| **Mentor** | `/api/v1/mentors/apply`| `POST` | `MentorApplyRequest(skills)` | `202 Accepted` |
| **Skill** | `/api/v1/skills` | `GET` | - | `List<SkillDto>` |
| **Session**| `/api/v1/sessions` | `POST` | `SessionRequest(mentorId, time)`| `SessionDto` |
| **Session**| `/api/v1/sessions/{id}`| `PATCH`| `SessionStatusUpdate(status)`| `SessionDto` |
| **Group** | `/api/v1/groups` | `POST` | `GroupRequest(name, topic)` | `GroupDto` |
| **Group** | `/api/v1/groups/{id}` | `GET` | - | `GroupDto` |
| **Group** | `/ws/chat` | `WS` | STOMP CONNECT frame | Live Socket |
| **Review** | `/api/v1/reviews` | `POST` | `ReviewRequest(sessionId, rate)`| `ReviewDto` |

---

## 3. Database Schema Design (ERD)

Each microservice governs its own logical PostgreSQL 16 schema. This ensures data sovereignty and prevents tight coupling.

```mermaid
erDiagram
    %% Auth & User Domain
    AUTH_USER {
        UUID id PK
        String email UK
        String password_hash
        String role "ENUM: USER, MENTOR, ADMIN"
    }
    USER_PROFILE {
        UUID id PK
        UUID auth_user_id FK UK
        String full_name
        String bio
        String avatar_url
    }

    %% Mentor & Skill Domain
    MENTOR_PROFILE {
        UUID id PK
        UUID user_id FK UK
        String expertise
        Boolean is_available
    }
    SKILL_CATALOG {
        UUID id PK
        String name UK
        String category
    }
    USER_SKILLS {
        UUID user_id FK
        UUID skill_id FK
        Integer proficiency
    }

    %% Session Domain
    MENTORSHIP_SESSION {
        UUID id PK
        UUID mentor_id FK
        UUID mentee_id FK
        Timestamp scheduled_time
        String status
    }

    %% Group & Chat Domain
    STUDY_GROUP {
        UUID id PK
        String name
        UUID creator_id FK
    }
    CHAT_MESSAGE {
        UUID id PK
        UUID group_id FK
        UUID sender_id FK
        Text message_content
        Timestamp sent_at
    }

    %% Review Service DB
    SESSION_REVIEW {
        UUID id PK
        UUID session_id FK UK
        UUID reviewer_id FK
        UUID reviewee_id FK
        Integer rating "1-5"
        Text feedback_comment
        Timestamp created_at
    }

    %% Relationships
    AUTH_USER ||--|| USER_PROFILE : "secures"
    USER_PROFILE ||--o| MENTOR_PROFILE : "extends to"
    USER_PROFILE ||--o{ USER_SKILLS : "has"
    SKILL_CATALOG ||--o{ USER_SKILLS : "categorizes"
    MENTOR_PROFILE ||--o{ MENTORSHIP_SESSION : "hosts"
    USER_PROFILE ||--o{ MENTORSHIP_SESSION : "books"
    STUDY_GROUP ||--o{ CHAT_MESSAGE : "contains"
    USER_PROFILE ||--o{ CHAT_MESSAGE : "authors"
    MENTORSHIP_SESSION ||--o| SESSION_REVIEW : "results in"
```

---

## 4. Class-Level Architecture & DTOs

Following Clean Architecture, services are decoupled internally. 
1.  **Controller (`SessionController.java`)**: Handles `/api/v1/sessions` requests. Executes `@Valid` on `SessionRequest`. Returns `ResponseEntity<SessionDto>`.
2.  **Service (`SessionServiceImpl.java`)**: Enforces business rules (e.g., checking if mentor is available). Uses `@Transactional` for database writes.
3.  **Repository (`SessionRepository.java`)**: Spring Data JPA interface. Provides methods like `findByMentorIdAndStatus(UUID, String)`.
4.  **Mapper (`SessionMapper.java`)**: Auto-generated MapStruct interfaces bridging `MentorshipSession` Entities and `SessionDto` output.

### 4.1 Global Exception Handling
- `@RestControllerAdvice` in `GlobalExceptionHandler.java` catches exceptions globally across a microservice.
- `MethodArgumentNotValidException` handles 400 Bad Requests mapping field errors to a standardized JSON schema.
- `ResourceNotFoundException` translates to 404 cleanly.

---

## 5. Security & JWT Workflow

Security relies on stateless verification to prevent database bottlenecking during traffic spikes.
1.  **Interception**: `JwtAuthenticationFilter` captures requests in downstream microservices.
2.  **Verification**: Using the `JWT_SECRET`, the filter verifies the token's cryptographic signature via `io.jsonwebtoken.Jwts`.
3.  **Context Construction**: Claims (`userId`, `roles`) are extracted into a `UsernamePasswordAuthenticationToken` and lodged in the `SecurityContextHolder`.
4.  **Authorization**: Controllers utilize method-level security (`@PreAuthorize("hasRole('ADMIN')")`) which evaluates the injected context before executing business logic.
5.  **Password Hashing**: The `auth-service` uses `BCryptPasswordEncoder` with a strength of 10 rounds to hash passwords before database persistence.

### 5.1 One-Time Password (OTP) Flow
- OTP generation leverages a secure random number generator (6 digits).
- Handled internally by `OtpService` in `auth-service`.
- State temporarily cached (could use Redis in production, currently in-memory concurrent map) with a 5-minute TTL.
- Emailed instantly via JavaMailSender synchronously for the user to proceed.

---

## 6. Service Discovery & Configuration Management

To ensure dynamic scaling, the platform avoids hardcoded IPs and properties.
- **Eureka Server**: Acts as the Service Registry (`:8761`). Services register on startup and send heartbeats every 30 seconds.
- **Client-Side Load Balancing**: Services use Spring Cloud LoadBalancer under the hood of Feign clients to resolve logical names (e.g., `http://mentor-service`) to physical dynamic IPs.
- **Spring Cloud Config**: A centralized `config-server` (`:8888`) connects to a Git repository or local filesystem. Microservices fetch their `application.yml` definitions upon startup based on their active profile (dev/prod).

---

## 7. Event-Driven Workflow (RabbitMQ)

To prevent the session creation API from hanging on external SMTP servers, notifications are entirely asynchronous.
- **Exchange**: `skillsync.topic`
- **Queue**: `notification.email.queue`
- **Algorithm**:
  1.  `SessionServiceImpl.createSession()` commits the session to Postgres.
  2.  `SessionMessagePublisher` converts a `SessionCreatedEvent` to JSON and publishes it to the Exchange with routing key `session.created`.
  3.  The API immediately returns a `201 Created` to the frontend.
  4.  Concurrently, `NotificationListener` inside `notification-service` pulls the event from the queue.
  5.  It constructs a HTML email using JavaMailSender and external SMTP properties.
  6.  **DLQ Mitigation**: If the SMTP server is down, RabbitMQ retries 3 times before routing the event to a Dead Letter Queue for manual admin intervention.

---

## 8. Real-Time WebSockets (Group Chat)

The `group-service` hosts a high-throughput STOMP server over WebSockets.
- **Connection**: Clients connect to `/ws/chat`. The `WebSocketAuthInterceptor` validates the JWT provided during the initial CONNECT frame headers.
- **Subscription Model**: Users subscribe to specific channels representing study groups: `/topic/group.{groupId}`.
- **Publishing Algorithm**:
  1.  A user emits a payload to `/app/chat.sendMessage`.
  2.  `ChatController` validates the sender's membership in the `group_id`.
  3.  The text is saved to the `CHAT_MESSAGE` table.
  4.  The server broadcasts the saved message DTO back to `/topic/group.{groupId}`, instantly updating all connected clients without HTTP polling.

---

## 9. Inter-Service Communication & Resilience

When microservices need synchronous data (e.g., `session-service` needing `mentor-service` data), Feign Clients are used.
- **Feign Interfaces**: Declarative HTTP clients (e.g., `@FeignClient(name = "mentor-service")`).
- **Resilience4j Circuit Breakers**: Wrapping Feign client calls to prevent cascading network failures.
- **Fallback Logic**: If the `mentor-service` is unreachable, the Circuit Breaker trips to `OPEN` state. It immediately returns a fallback cache or a fast `503 Service Unavailable` rather than holding the HTTP thread open until timeout, thereby protecting the overall system stability.

---

## 10. Tracing & Observability

To monitor requests traversing multiple microservices, distributed tracing is implemented via Zipkin and Micrometer.
- **Trace Propagation**: When the API Gateway receives a request, it generates a unique `Trace-Id`. This ID is injected into HTTP headers (`X-B3-TraceId`) for all subsequent internal REST calls.
- **Log Aggregation**: Application logs include the `[Service, Trace-Id, Span-Id]`.
- **Zipkin Dashboard**: All latency metrics and spans are exported to the Zipkin container (`:9411`). Developers can visually trace a request starting from the `gateway`, hopping to the `session-service`, and completing an internal Feign call to the `mentor-service`.
