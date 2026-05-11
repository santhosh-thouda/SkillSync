# SkillSync: Enterprise Microservices Platform - Low-Level Design (LLD)

## 1. Document Metadata & Introduction

| Version | Date | Author | Description |
| :--- | :--- | :--- | :--- |
| 1.0 | 2026-05-03 | Antigravity AI | Initial comprehensive LLD for SkillSync Ecosystem |
| 1.1 | 2026-05-03 | Antigravity AI | Expanded with granular DTO, Entity, and Implementation logic |
| 1.2 | 2026-05-03 | Antigravity AI | Deep-dive into every microservice component and API contract |

---

## 2. Introduction
The Low-Level Design (LLD) document provides an exhaustive, granular analysis of the internal workings of the SkillSync microservices ecosystem. While the HLD focused on the "What" and the "How" at a system level, this document dives into the "Exactly How"—detailing class structures, specific implementation logic, API contracts, database schemas, and inter-service communication protocols. This document serves as the primary implementation blueprint for developers and engineers.

---

## 3. Shared Infrastructure & Technical Patterns

### 3.1 Service Discovery Implementation (Netflix Eureka)
- **Module**: `eureka-server`
- **Main Class**: `EurekaServerApplication` annotated with `@EnableEurekaServer`.
- **Logic**: 
    - Maintains an in-memory registry of service instances.
    - Uses a peer-to-peer replication strategy (if configured for multi-node).
    - Exposes a dashboard at `:8761` for real-time monitoring.
- **Client Configuration**: 
    - Services use `spring-cloud-starter-netflix-eureka-client`.
    - Property `eureka.client.service-url.defaultZone` points to the Eureka server.
    - Heartbeat interval: 30 seconds (default).

### 3.2 Centralized Configuration (Config Server)
- **Module**: `config-server`
- **Main Class**: `ConfigServerApplication` annotated with `@EnableConfigServer`.
- **Backend Strategy**: Configured to pull from a local directory (`config-repo`) for development, easily switchable to a Git URI for production.
- **Property Resolution**: 
    - Services fetch configuration using `{application-name}-{profile}.yml`.
    - Logic: Service Bootstrap -> Connect to 8888 -> Identify application name -> Merge properties -> Initialize Spring Context.

### 3.3 API Gateway & Routing (Spring Cloud Gateway)
- **Module**: `api-gateway`
- **Routing Logic**:
    - Uses `RouteLocatorBuilder` or YAML-based configuration.
    - **Predicates**: Match incoming request paths (e.g., `Path=/auth/**`).
    - **Filters**: Modify requests (e.g., `AddRequestHeader`, `RewritePath`).
- **Resilience**: Integrated with Resilience4j for gateway-level circuit breaking.
- **Security**: Permissive configuration (`anyExchange().permitAll()`) because security is delegated to individual microservices for fine-grained control.

---

## 4. Identity & Access Management: Auth Service (:8081)

### 4.1 Data Transfer Objects (DTOs)

#### 4.1.1 `RegisterRequest`
- **Purpose**: Encapsulates data required for new user registration.
- **Fields**:
    - `String name`: Full name of the user.
    - `String email`: Unique email address (Primary identifier).
    - `String password`: Plaintext password (to be hashed).
    - `String role`: User role (e.g., ROLE_LEARNER, ROLE_MENTOR).

#### 4.1.2 `LoginRequest`
- **Purpose**: Credentials for authentication.
- **Fields**:
    - `String email`: Registered email.
    - `String password`: Plaintext password.

#### 4.1.3 `AuthResponse`
- **Purpose**: Payload returned upon successful authentication.
- **Fields**:
    - `String token`: Signed JWT.
    - `Long userId`: Database ID of the user.
    - `String role`: Assigned role.

#### 4.1.4 `RefreshRequest`
- **Purpose**: Request to issue a new JWT using a valid old token.
- **Fields**:
    - `String token`: The existing JWT.

#### 4.1.5 `UserSyncRequest`
- **Purpose**: Internal DTO used to sync auth data to the User Service.
- **Fields**:
    - `Long id`: Auth Service user ID.
    - `String name`: User's name.
    - `String email`: User's email.
    - `String role`: User's role.

#### 4.1.6 `MentorSyncRequest`
- **Purpose**: Internal DTO used to sync auth data to the Mentor Service.
- **Fields**:
    - `Long userId`: Auth Service user ID.
    - `String name`: User's name.

### 4.2 Security Components

#### 4.2.1 `JwtAuthenticationFilter`
- **Logic**:
    1. Extracts `Authorization` header.
    2. Validates `Bearer` token.
    3. If valid, populates `SecurityContextHolder`.

#### 4.2.2 `JwtUtil`
- **Secret Management**: Derived from `jwt.secret` property.
- **Methods**:
    - `generateToken(String email, Long userId, String role)`
    - `extractEmail(String token)`
    - `extractUserId(String token)`
    - `extractRole(String token)`
    - `isTokenValid(String token, String email)`

---

## 5. User Management: User Service (:8082)

### 5.1 Data Transfer Objects (DTOs)

#### 5.1.1 `UserDto`
- **Fields**:
    - `Long id`: Profile ID.
    - `String name`: Full name.
    - `String email`: Email.
    - `String bio`: User biography.
    - `String profileImage`: URL to profile picture.
    - `String role`: Role.

#### 5.1.2 `UserUpdateRequest`
- **Fields**:
    - `String name`: Updated name.
    - `String bio`: Updated bio.
    - `String profileImage`: Updated image URL.

---

## 6. Professional Expertise: Mentor Service (:8083)

### 6.1 Data Transfer Objects (DTOs)

#### 6.1.1 `MentorDto`
- **Fields**:
    - `Long id`: Mentor profile ID.
    - `Long userId`: Link to Auth ID.
    - `String name`: Mentor's name.
    - `String bio`: Professional bio.
    - `Integer experience`: Years of experience.
    - `List<String> skills`: List of skills.
    - `Double hourlyRate`: Rate per hour.
    - `Boolean available`: Availability status.
    - `Boolean approved`: Approval status.

#### 6.1.2 `MentorApplyRequest`
- **Fields**:
    - `Long userId`: User ID.
    - `String bio`: Bio.
    - `Integer experience`: Years of experience.
    - `List<String> skills`: Requested skills.
    - `Double hourlyRate`: Requested rate.

#### 6.1.3 `MentorUpdateRequest`
- **Fields**:
    - `String bio`: Updated bio.
    - `Integer experience`: Updated experience.
    - `List<String> skills`: Updated skills.
    - `Double hourlyRate`: Updated rate.

#### 6.1.4 `AvailabilityUpdateRequest`
- **Fields**:
    - `Boolean available`: New availability state.

---

## 7. Learning Sessions: Session Service (:8085)

### 7.1 Data Transfer Objects (DTOs)

#### 7.1.1 `SessionDto`
- **Fields**:
    - `Long id`: Session ID.
    - `Long mentorId`: Mentor ID.
    - `String mentorName`: Mentor Name.
    - `Long learnerId`: Learner ID.
    - `String learnerName`: Learner Name.
    - `LocalDateTime sessionDate`: Scheduled time.
    - `String status`: Current status (REQUESTED, ACCEPTED, etc.).

#### 7.1.2 `SessionRequest`
- **Fields**:
    - `Long mentorId`: Target mentor.
    - `Long learnerId`: Requesting learner.
    - `LocalDateTime sessionDate`: Proposed time.

---

## 8. Topic Management: Skill Service (:8084)

### 8.1 Data Transfer Objects (DTOs)

#### 8.1.1 `SkillDto`
- **Fields**:
    - `Long id`: Skill ID.
    - `String name`: Skill name.
    - `String category`: Skill category.

#### 8.1.2 `SkillRequest`
- **Fields**:
    - `String name`: New skill name.
    - `String category`: New skill category.

---

## 9. Community Interaction: Group Service (:8086)

### 9.1 Data Transfer Objects (DTOs)

#### 9.1.1 `GroupDto`
- **Fields**:
    - `Long id`: Group ID.
    - `String name`: Group name.
    - `String description`: Description.
    - `Set<Long> memberIds`: IDs of members.
    - `Long createdBy`: Owner ID.

#### 9.1.2 `GroupRequest`
- **Fields**:
    - `String name`: Name.
    - `String description`: Description.
    - `Long createdBy`: Creator ID.

#### 9.1.3 `ChatMessageDto`
- **Fields**:
    - `Long id`: Message ID.
    - `Long groupId`: Group ID.
    - `Long senderId`: Sender ID.
    - `String content`: Message text.
    - `LocalDateTime timestamp`: Time sent.

#### 9.1.4 `SendMessageRequest`
- **Fields**:
    - `Long senderId`: Sender ID.
    - `String content`: Message text.

---

## 10. Feedback Loop: Review Service (:8087)

### 10.1 Data Transfer Objects (DTOs)

#### 10.1.1 `ReviewDto`
- **Fields**:
    - `Long id`: Review ID.
    - `Long mentorId`: Mentor ID.
    - `Long reviewerId`: Reviewer ID.
    - `Integer rating`: Stars (1-5).
    - `String comment`: Textual feedback.
    - `LocalDateTime createdAt`: Time created.

#### 10.1.2 `ReviewRequest`
- **Fields**:
    - `Long mentorId`: Target mentor.
    - `Long reviewerId`: User giving review.
    - `Integer rating`: Rating.
    - `String comment`: Feedback.

---

## 11. Database Schema Specifications (Complete)

### 11.1 Auth Service (`auth_db`)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

### 11.2 User Service (`user_db`)
```sql
CREATE TABLE users_profile (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    bio TEXT,
    profile_image VARCHAR(255),
    role VARCHAR(50)
);
```

### 11.3 Mentor Service (`mentor_db`)
```sql
CREATE TABLE mentors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    bio TEXT,
    experience INTEGER,
    hourly_rate DECIMAL(10,2),
    available BOOLEAN DEFAULT TRUE,
    approved BOOLEAN DEFAULT FALSE
);

CREATE TABLE mentor_skills (
    mentor_id BIGINT NOT NULL,
    skill_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (mentor_id) REFERENCES mentors(id)
);
```

### 11.4 Session Service (`session_db`)
```sql
CREATE TABLE mentorship_sessions (
    id BIGSERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL,
    learner_id BIGINT NOT NULL,
    session_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL
);
```

### 11.5 Skill Service (`skill_db`)
```sql
CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    category VARCHAR(255) NOT NULL
);
```

### 11.6 Group Service (`group_db`)
```sql
CREATE TABLE study_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL
);

CREATE TABLE group_memberships (
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES study_groups(id)
);

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES study_groups(id)
);
```

### 11.7 Review Service (`review_db`)
```sql
CREATE TABLE mentor_reviews (
    id BIGSERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 12. Detailed Implementation Logic: Key Workflows

### 12.1 User Registration & Cascading Sync
- **Service**: `AuthService`
- **Method**: `register(RegisterRequest)`
- **Steps**:
    1. Validate email uniqueness in `UserRepository`.
    2. Hash password using `BCryptPasswordEncoder`.
    3. Save `User` entity to `auth_db`.
    4. Call `UserServiceClient.createUser` via Feign.
    5. If role is `MENTOR`, call `MentorServiceClient.createMentor`.
    6. Return success.

### 12.2 Session Booking & Status Lifecycle
- **Service**: `SessionService`
- **Methods**:
    - `requestSession(SessionRequest)`: Validates mentor availability -> Persists as `REQUESTED` -> Publishes RabbitMQ event.
    - `acceptSession(Long id)`: Checks if current user is the mentor -> Updates to `ACCEPTED` -> Publishes RabbitMQ event.
    - `completeSession(Long id)`: Updates to `COMPLETED`.

### 12.3 Mentor Application & Approval
- **Service**: `MentorService`
- **Methods**:
    - `applyForMentor(MentorApplyRequest)`: Creates profile with `approved = false`.
    - `approveMentor(Long id)`: Admin sets `approved = true`. This enables the mentor to appear in search results.

---

## 13. Security Configuration Details

### 13.1 `SecurityConfig` (Shared Pattern)
- **CSRF**: Disabled (Stateless).
- **Session Management**: `SessionCreationPolicy.STATELESS`.
- **Filters**: `JwtAuthenticationFilter` added before `UsernamePasswordAuthenticationFilter`.
- **Method Security**: `@EnableMethodSecurity` allows for `@PreAuthorize` annotations.

---

## 14. Testing Architecture

### 14.1 Unit Testing
- **Framework**: JUnit 5, Mockito.
- **Coverage Target**: 80%+ of Service layer.
- **Pattern**: 
    - Mock Repositories.
    - Mock Feign Clients.
    - Use `AssertJ` for fluent assertions.

### 14.2 Integration Testing
- **Framework**: Spring Boot Test.
- **MockMvc**: For testing Controller endpoints without a full server.

---

## 15. Conclusion
This LLD provides the exhaustive technical blueprint for the SkillSync platform. By detailing every DTO, Entity, SQL schema, and core logic block, it ensures that the development team has a clear and unambiguous path for implementing, testing, and maintaining the system's robust microservices architecture.

---
*(End of Low-Level Design Document)*
