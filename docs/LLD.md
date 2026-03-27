# SkillSync Low-Level Design (LLD)

## 1. Introduction
The Low-Level Design (LLD) provides a granular, file-by-file analysis of the SkillSync microservices ecosystem. It focuses on the internal logic, class responsibilities, API endpoint signatures, and data transformation patterns that drive the system's business operations.

---

## 2. Shared Infrastructure & Patterns

### 2.1 Service Registration & Discovery (Eureka)
- **Component**: `EurekaServerApplication` (eureka-server).
- **Function**: Acts as a Registry. Downstream services use `@EnableDiscoveryClient` to broadcast their presence.
- **Why**: decouples service locations from their logic, allowing dynamic scaling.
- **How**: Services register via `application.properties` and keep-alive heartbeats.
- **Flow**: Service starts -> Registry Lookup -> Client-side Load Balancing.

### 2.2 API Gateway & Routing
- **Component**: `ApiGatewayApplication` (api-gateway).
- **Configuration**: `GatewaySecurityConfig`.
- **Function**: Aggregates all service routes into a single port (:8080).
- **Why**: Simplifies client-side development and centralizes cross-cutting concerns (CORS, JWT validation).
- **Flow**: Client (:8080) -> Predicate (Path) Match -> Downstream Forwarding.

---

## 3. Identity Core: Auth & User Services

### 3.1 Auth Service (:8081)
The Auth Service is the primary gatekeeper of the system, handling identity verification and token generation.

#### 3.1.1 Controllers
- **`AuthController`**:
    - `POST /auth/register`: Receives `RegisterRequest`. Calls `authService.register()`. Returns 201 Created.
    - `POST /auth/login`: Receives `LoginRequest`. Returns `AuthResponse` with JWT.
    - `POST /auth/refresh`: Receives `RefreshRequest`. Returns new `AuthResponse`.

#### 3.1.2 Service Layer (`AuthService`)
- **`register()`**:
    - **Logic**: Encrypts password -> Saves User entity -> Initiates `syncRegistration()`.
    - **Cascade**: Uses `UserServiceClient` and `MentorServiceClient` (Feign) to create initial profiles.
    - **Rollback**: If synchronization fails, the Auth record is deleted to ensure consistency.
- **`login()`**:
    - **Logic**: Validates email -> Compares BCrypt passwords -> Generates JWT via `JwtUtil`.

#### 3.1.3 Security & Utility
- **`JwtUtil`**: Encapsulates JWTS logic (signing, parsing).
- **`JwtAuthenticationFilter`**: Intercepts requests, validates the 'Authorization' bearer token, and populates the `SecurityContextHolder`.

---

### 3.2 User Service (:8082)
Manages learner profiles and administrative user data.

#### 3.2.1 Service Implementation (`UserServiceImpl`)
- **`createUser()`**: Persists a new `User` entity received from a synchronization request.
- **`updateUser()`**: Updates bio and profile image.
- **`getUserById()` / `getUserByEmail()`**: Standard CRUD operations.

#### 3.2.2 Mapping Logic (`UserMapper`)
- **Pattern**: Manual mapping from `User` entity to `UserDto`.
- **Reason**: Decouples the database schema from the API contract, allowing for field redaction (e.g., hiding passwords).

---

## 4. Domain Layer: Mentorship & Skills

### 4.1 Mentor Service (:8083)
Facilitates the mentor lifecycle from application to availability management.

#### 4.1.1 Core Components
- **`MentorController`**:
    - `POST /mentors/apply`: Learners apply to become mentors.
    - `PUT /mentors/{id}/approve`: ADMIN-only endpoint to verify mentors.
    - `PATCH /mentors/{id}/availability`: Mentors toggle their `available` flag.
- **`MentorServiceImpl`**:
    - Implements business rules for mentor approval and skill mapping.

---

### 4.2 Skill Service (:8084)
The canonical source for all learning topics in the platform.

#### 4.2.1 Logic Flow
- **`createSkill()`**: Ensures skill name uniqueness (case-insensitive).
- **`getAllSkills()`**: Returns the full catalog for UI dropdowns.

---

## 5. Event-Driven Messaging: Session & Notifications

### 5.1 Session Service (:8085)
The engine for booking and managing mentorship interactions.

#### 5.1.1 Event Publishing
- **`SessionMessagePublisher`**:
    - **Mechanism**: Serializes `SessionEvent` and sends to RabbitMQ.
    - **Why**: Ensures notifications are handled out-of-band, keeping the booking request fast.
- **Logic**: On status update (REQUESTED -> ACCEPTED), an event is automatically emitted.

---

## 6. Community & Feedback: Group & Review

### 6.1 Group Service (:8086)
- **`GroupController`**:
    - `POST /groups`: Creates a new study group.
    - `POST /groups/{id}/join`: Adds a user ID to the `members` list.
- **`GroupServiceImpl`**: Manages the membership state in the `StudyGroup` entity.

### 6.2 Review Service (:8087)
- **`ReviewController`**:
    - `POST /reviews`: Submit a rating (1-5) and comment for a mentor.
    - `GET /reviews/mentor/{id}`: Fetch all reviews to calculate social proof.

---

## 7. The Master List of "The Single Single Things"

### 7.1 Lombok (`org.projectlombok`)
- **Why**: To eliminate verbose boilerplate (getters, setters, constructors, builders).
- **Where**: Annotating virtually every Entity, DTO, and Service in the system.
- **How**: Annotations like `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder`.
- **Flow**: During compilation, Lombok generates the bytecode for these methods, keeping the source code clean.

### 7.2 Hibernate & JPA
- **Why**: To map Java objects to relational database tables without writing raw SQL.
- **Where**: All services with database persistence (Auth, User, Mentor, etc.).
- **How**: Using `@Entity` on classes and extending `JpaRepository`.
- **Flow**: Java Object -> JPA EntityManager -> Hibernate Dialect -> PostgreSQL SQL Query.

### 7.3 Spring Cloud Gateway
- **Why**: Centralized routing and cross-cutting concern management.
- **Where**: `api-gateway` module.
- **How**: Route definitions in `application.properties` using `lb://SERVICE-ID` format.
- **Flow**: Client Request -> Route Predicate -> Discovery Match -> Load Balancer -> Forward.

### 7.4 Netflix Eureka
- **Why**: Dynamic service discovery for ephemeral containerized instances.
- **Where**: `eureka-server` and all clients.
- **How**: `@EnableEurekaServer` and `@EnableDiscoveryClient`.
- **Flow**: Service Up -> Register with Eureka -> Heartbeat Sent -> Available to Gateway.

### 7.5 Spring Cloud OpenFeign
- **Why**: Declarative REST client implementation.
- **Where**: `AuthService` (calling User/Mentor).
- **How**: `@FeignClient(name = "user-service")`.
- **Flow**: Interface Call -> Dynamic Proxy -> Eureka Lookup -> HTTP Execute.

### 7.6 io.jsonwebtoken (JJWT)
- **Why**: To generate and verify secure, stateless authentication tokens.
- **Where**: `JwtUtil` in almost every service.
- **How**: `Jwts.builder()` for signing and `Jwts.parser()` for extracting claims.
- **Flow**: User Login -> JJWT signs payload -> Token returned -> JJWT validates signature on subsequent calls.

### 7.7 Resilience4j
- **Why**: Fault tolerance and prevention of cascading failures.
- **Where**: `MentorServiceClient`.
- **How**: `@CircuitBreaker` with custom configuration in `application.properties`.
- **Flow**: Request Fails -> Threshold reached -> Circuit OPENS -> Fast fail/Fallback -> Half-Open check.

### 7.8 Micrometer Tracing & Brave
- **Why**: To propagate and collect distributed trace IDs.
- **Where**: Core dependency across all services and the Gateway.
- **How**: Injects `traceId` and `spanId` into HTTP headers.
- **Flow**: Gateway Request -> Trace ID created -> Passed to Auth Service -> Logged -> Exported to Zipkin.

### 7.9 OpenZipkin
- **Why**: Visualization of distributed request lifecycles.
- **Where**: Standalone container (:9412).
- **How**: Services export spans via `zipkin-reporter-brave`.
- **Flow**: Trace Completed -> Exported to Zipkin -> Queryable via UI.

### 7.10 RabbitMQ (Spring AMQP)
- **Why**: Asynchronous, reliable delivery of notification events.
- **Where**: `session-service` (Publisher) and `notification-service` (Listener).
- **How**: `RabbitTemplate` for sending and `@RabbitListener` for receiving.
- **Flow**: Event Published -> Topic Exchange -> Queue Binding -> Consumer Processing.

### 7.11 BCrypt (Spring Security Crypto)
- **Why**: Secure one-way hashing of user passwords.
- **Where**: `AuthService`.
- **How**: `PasswordEncoder.encode()` and `matches()`.
- **Flow**: Raw Password -> Salted Hash -> Persisted in DB.

### 7.12 SpringDoc OpenAPI (Swagger)
- **Why**: Automated API documentation and interactive testing sandbox.
- **Where**: All services, aggregated at the Gateway.
- **How**: `springdoc-openapi-starter-webmvc-ui`.
- **Flow**: Reflection on Controllers -> OpenAPI Spec generated -> Rendered as Swagger UI.

### 7.13 Mockito
- **Why**: Isolate business logic from external dependencies during unit tests.
- **Where**: `src/test/java` in each service.
- **How**: `@Mock` for repositories/clients, `Mockito.verify()`.
- **Flow**: Setup mocks -> Invoke service -> Assert result -> Verify interactions.

---

## 8. Inter-Service Communication & Messaging Matrix

| Producer Service | Consumer Service | Pattern | Event / Action |
| :--- | :--- | :--- | :--- |
| **Auth** | **User** | Sync (Feign) | `createUser` on registration |
| **Auth** | **Mentor** | Sync (Feign) | `createMentor` on registration |
| **Gateway** | **Any** | Sync (HTTP) | Forward client request |
| **Session** | **Notification** | Async (Rabbit) | `session.requested` event |
| **Session** | **Notification** | Async (Rabbit) | `session.status_changed` event |
| **Auth** | **User** | Sync (Feign) | `refresh` (token validation lookup) |

---

## 8. Exhaustive API Reference

### 8.1 Auth Service (:8081)
- **`POST /auth/register`**
    - **Request**: `RegisterRequest { name, email, password, role }`
    - **Response**: `201 Created` - "User registered successfully"
    - **Auth**: Public
- **`POST /auth/login`**
    - **Request**: `LoginRequest { email, password }`
    - **Response**: `AuthResponse { token, userId, role }`
    - **Auth**: Public
- **`POST /auth/refresh`**
    - **Request**: `RefreshRequest { token }`
    - **Response**: `AuthResponse`
    - **Auth**: Public (Valid Token required)

### 8.2 User Service (:8082)
- **`GET /users/{id}`**
    - **Auth**: ADMIN or Self
    - **Response**: `UserDto`
- **`GET /users/email/{email}`**
    - **Auth**: ADMIN or Self
    - **Response**: `UserDto`
- **`PUT /users/{id}`**
    - **Request**: `UserUpdateRequest { name, profileImage }`
    - **Auth**: ADMIN or Self
- **`DELETE /users/{id}`**
    - **Auth**: ADMIN or Self

### 8.3 Mentor Service (:8083)
- **`POST /mentors/apply`**
    - **Request**: `MentorApplyRequest { userId, bio, experience, skills, hourlyRate }`
    - **Auth**: Authenticated Learner
- **`GET /mentors`**
    - **Auth**: Public
    - **Response**: `List<MentorDto>`
- **`PUT /mentors/{id}/approve`**
    - **Auth**: ADMIN
- **`PATCH /mentors/{id}/availability`**
    - **Request**: `AvailabilityUpdateRequest { available: boolean }`
    - **Auth**: Self (Mentor)

### 8.4 Session Service (:8085)
- **`POST /sessions/request`**
    - **Request**: `SessionRequest { mentorId, learnerId, sessionDate }`
    - **Auth**: Learner
- **`PATCH /sessions/{id}/status`**
    - **Params**: `status` (ACCEPTED, REJECTED, COMPLETED)
    - **Auth**: Mentor (for Accept/Reject) or Learner (for Request/Complete)

### 8.5 Group Service (:8086)
- **`POST /groups`**
    - **Request**: `GroupRequest { name, description, createdBy }`
- **`POST /groups/{id}/join`**
    - **Auth**: Any Authenticated User
- **`GET /groups`**
    - **Auth**: Authenticated

---

## 9. Data Model & Entity Specifications

### 9.1 Auth - `User` Entity
- `Long id`: Primary Key (Auto-increment).
- `String name`: Non-null.
- `String email`: Unique, Index.
- `String password`: BCrypt Hashed.
- `String role`: Enum-like String (ADMIN, MENTOR, LEARNER).

### 9.2 Mentor - `Mentor` Entity
- `Long id`: Primary Key.
- `Long userId`: Logical Foreign Key to Auth Service.
- `String bio`: Professional summary.
- `Integer experience`: Years of experience.
- `Double hourlyRate`: Pricing per session.
- `Boolean available`: Current status.
- `List<String> skills`: Collection of skill names.

### 9.3 Notification - `Notification` Entity
- `Long id`: PK.
- `Long userId`: Target user.
- `String message`: Notification body.
- `LocalDateTime createdAt`: Timestamp.

---

## 10. Security Filter Implementation Detail
The `JwtAuthenticationFilter` is the backbone of the system's security.

### 10.1 `doFilterInternal` Logic Flow
1.  **Extract Header**: Check for `Authorization` header starting with `Bearer `.
2.  **Validate Token**: Call `JwtUtil.validateToken()`.
3.  **Authentication Object**: If valid, extract `email` and `role` from claims.
4.  **Security Context**: Create a `UsernamePasswordAuthenticationToken` and set it in the `SecurityContextHolder`.
5.  **Chain Continue**: Proceed to the next filter in the Spring Security chain.

### 10.2 `SecurityConfig` Configuration
- **Stateless Session**: `.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`
- **CSRF Disabled**: Necessary for non-browser clients and stateless JWT usage.
- **Path Matchers**: 
    - `/auth/**` -> `.permitAll()`
    - `/api-docs/**` -> `.permitAll()`
    - All other paths -> `.authenticated()`

---

## 11. Testing Strategies (LLD View)

### 11.1 Unit Testing with Mockito
Each service layer (e.g., `AuthService`) is tested in isolation using:
- **`@Mock`**: For repositories and Feign clients.
- **`@InjectMocks`**: For the service under test.
- **`verify()`**: To ensure that the repository `save()` or Feign `createUser()` were called exactly once.
- **`when().thenReturn()`**: To simulate database responses or external API success/failure.

### 11.2 Integration Testing (Testcontainers)
For deep logic validation, the system uses Spring Boot's `@SpringBootTest` with dynamic properties to mock the PostgreSQL and RabbitMQ environments.

---

## 12. Exception Handling Infrastructure

### 12.1 `GlobalExceptionHandler`
- **`@ControllerAdvice`**: Intercepts all exceptions thrown from Controllers.
- **Custom Exceptions**:
    - `ResourceNotFoundException`: Returns 404.
    - `BadRequestException`: Returns 400.
    - `UnauthorizedException`: Returns 401.
- **Response Format**:
    ```json
    {
      "timestamp": "2026-03-27T...",
      "message": "User not found with id 1",
      "status": 404
    }
    ```

---

## 13. Logging & Strategic Monitoring
Every service uses the same logging pattern to facilitate distributed tracing.

### 13.1 `Logback` Configuration
- **Pattern**: `[%d{yyyy-MM-dd HH:mm:ss}] [%thread] [%-5level] [%logger{36}] [%X{traceId:-},%X{spanId:-}] - %msg%n`
- **Distribution**: 
    - **Info**: Startup details, successful transactions.
    - **Error**: Stack traces for 5xx errors and failed Feign calls.
    - **Debug**: Security filtering details (selective).

---

## 15. Domain Service Detailed Analysis

### 15.1 Skill Service (:8084)
- **`SkillController`**:
    - `POST /skills`: Create a new skill.
    - `GET /skills`: List all.
- **`SkillServiceImpl`**:
    - **Logic**: Uses `findByNameIgnoreCase` to prevent duplicates. If a skill exists, it returns the existing one instead of creating a new one (Idempotency).
- **`SkillMapper`**: Maps `Skill` entity (id, name, category) to `SkillDto`.

### 15.2 Group Service (:8086)
- **`GroupController`**:
    - `POST /groups`: Admin/Mentor creates a group.
    - `POST /groups/{id}/join`: Current user joins.
    - `POST /groups/{id}/leave`: Current user leaves.
- **`GroupServiceImpl`**:
    - **Logic**: Manages a `Set<Long> members` to track participants. Uses `@Transactional` to ensure state consistency during join/leave operations.

### 15.3 Review Service (:8087)
- **`ReviewController`**:
    - `POST /reviews`: Submit a rating.
    - `GET /reviews/mentor/{id}`: Aggregate feedback.
- **`ReviewServiceImpl`**:
    - **Logic**: Persists ratings (1-5 range) and links them to a `mentor_id` and `user_id`.

### 15.4 Notification Service (:8088)
- **`NotificationListener`**:
    - **Pattern**: RabbitMQ consumer using `@RabbitListener`.
    - **Logic**: Receives `SessionEvent`, parses the status, and "sends" a notification (currently logs the payload to the console).
- **`NotificationService`**: Saves a history of notifications sent to each user in the `notifications` table.

---

## 16. Database Schema Design (Detailed)

### 16.1 Auth Service (`auth_db`)
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

### 16.2 User Service (`user_db`)
```sql
CREATE TABLE users_profile (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    bio TEXT,
    profile_image VARCHAR(255),
    role VARCHAR(50)
);
```

### 16.3 Mentor Service (`mentor_db`)
```sql
CREATE TABLE mentors (
    id SERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE,
    bio TEXT,
    experience INT,
    hourly_rate DECIMAL(10,2),
    available BOOLEAN DEFAULT TRUE,
    approved BOOLEAN DEFAULT FALSE
);

CREATE TABLE mentor_skills (
    mentor_id BIGINT,
    skill_name VARCHAR(255),
    FOREIGN KEY (mentor_id) REFERENCES mentors(id)
);
```

### 16.4 Session Service (`session_db`)
```sql
CREATE TABLE mentorship_sessions (
    id SERIAL PRIMARY KEY,
    mentor_id BIGINT,
    learner_id BIGINT,
    session_date TIMESTAMP,
    status VARCHAR(50) -- REQUESTED, ACCEPTED, REJECTED, COMPLETED
);
```

---

## 17. Security Implementation: `AccessControlService`
Used in `@PreAuthorize` annotations across controllers to implement fine-grained logic.

- **`isCurrentUser(Long id, Authentication auth)`**: Compares the requested ID with the `userId` claim in the JWT.
- **`hasEmail(Authentication auth, String email)`**: Compares the requested email with the `sub` claim.
- **`canCreateUser(UserDto dto, Authentication auth)`**: Ensures that only ADMINs can create any user, while others can only register via the Auth service.

---

## 18. Distributed Tracing: The Span Lifecycle
1.  **Client Request**: Gateway receives request.
2.  **Span Initiation**: Brave creates a new `Trace ID`.
3.  **Propagation**: Trace ID added to `X-B3-TraceId` header.
4.  **Downstream**: Auth Service receives header, creates a child `Span ID`.
5.  **Logging**: Every log line includes `[TraceID, SpanID]`.
6.  **Export**: Background reporter sends spans to Zipkin (:9412).

---

## 19. Performance Best Practices in Code
- **Streams API**: Extensively used for mapping Collections (DTO <-> Entities).
- **Lombok `@RequiredArgsConstructor`**: Used for constructor-based dependency injection (Spring's recommended pattern).
- **Transaction Management**: `@Transactional` used on service methods that modify state to ensure atomic operations.
- **Validation**: `@Valid` and JSR-303 annotations (`@NotNull`, `@Email`) ensure data integrity at the Controller edge.

---

## 21. Major Data Transfer Objects (DTO) Specification

### 21.1 Identity DTOs
- **`RegisterRequest`**:
    - `String name`: User's full name.
    - `String email`: Validated email address.
    - `String password`: Plain text (encrypted before storage).
    - `String role`: Enum (MENTOR, LEARNER).
- **`AuthResponse`**:
    - `String token`: Bearer JWT.
    - `Long userId`: Internal database ID.
    - `String role`: Role for frontend routing.

### 21.2 Mentor DTOs
- **`MentorDto`**:
    - `Long id`: Mentor profile ID.
    - `Long userId`: Reference to Auth User.
    - `String bio`: Professional summary.
    - `Double hourlyRate`: Session cost.
    - `List<String> skills`: List of expertise areas.
    - `Boolean available`: Current mentor status.

### 21.3 Session DTOs
- **`SessionDto`**:
    - `Long id`: Session ID.
    - `Long mentorId`: ID of the mentor.
    - `Long learnerId`: ID of the learner.
    - `LocalDateTime sessionDate`: Scheduled time.
    - `String status`: Cycle (REQUESTED -> ACCEPTED -> ...).

---

## 22. Exhaustive Configuration Reference (`application.properties`)

| Property Key | Value / Example | Purpose |
| :--- | :--- | :--- |
| `spring.application.name` | `auth-service` | Service logical ID for Eureka |
| `server.port` | `8081` | Local port binding |
| `eureka.client.service-url` | `http://...:8761/eureka` | Eureka registry endpoint |
| `jwt.secret` | `${JWT_SECRET}` | Secret key for JJWT signing |
| `resilience4j.circuitbreaker.failureRateThreshold` | `50` | Failure rate to open circuit |
| `management.tracing.sampling.probability` | `1.0` | Zipkin sampling rate (100%) |

---

## 23. Deployment Hierarchy (Docker Compose)
The `docker-compose.yml` file defines the entire ecosystem.

- **Services**: 10 Microservices + PostgreSQL + RabbitMQ + Zipkin.
- **Networks**:
    - `skillsync-network`: Custom bridge network for inter-service talk.
- **Volumes**:
    - `postgres-data`: Persistent storage for the database container.
- **Environment Management**:
    - Centralized `.env` file feeds all secrets (DB credentials, JWT keys) into containers at runtime.

---

## 24. Conclusion
The SkillSync project represents a highly structured, scalable, and maintainable implementation of a Mentorship Platform. By adhering to microservices best practices—such as database isolation, circuit breaking, and distributed tracing—the system is prepared for high-traffic, production-grade workloads while remaining easy for developers to extend and debug.
