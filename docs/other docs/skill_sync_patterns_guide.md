# SkillSync Design Patterns: Saga & Outbox

This guide explains how advanced distributed system patterns like the **Saga Pattern** and **Outbox Pattern** are applied (or planned) in the SkillSync microservices architecture.

## 1. Saga Pattern (Choreography-based)

### The Problem
In a microservices world, a single business action (like User Registration) often requires updating multiple databases. Since we cannot use traditional database transactions across different services, we use a **Saga**.

### Usage in SkillSync: Registration Flow
The `Auth Service` acts as the orchestrator for the registration saga.

1.  **Local Transaction**: `Auth Service` saves the user's credentials (email/password) in its own database.
2.  **Downstream Call**: It then calls the `User Service` (for Learners) or `Mentor Service` (for Mentors) synchronously via Feign to create their profile.
3.  **Compensation (Rollback)**: If the downstream call fails (e.g., `user-service` is down), the `Auth Service` catches the error and **deletes** the credential record it just created.

**Why this matters**: This prevents "zombie accounts" where a login exists but the profile is missing.

**File to Explore**: [AuthService.java (auth-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/auth-service/src/main/java/com/capgemini/auth/service/AuthService.java#L60-L78)

---

## 2. Outbox Pattern

### The Problem
When a service publishes a message to RabbitMQ (like `Session Service` sending a notification), two things happen:
1.  The database is updated (e.g., Session status changed to `ACCEPTED`).
2.  A message is sent to RabbitMQ.

If the database update succeeds but RabbitMQ is down, the notification is lost. If RabbitMQ succeeds but the database fails, the systems are out of sync.

### Implementation in SkillSync
In the current version, the project uses **Direct Publishing**. However, the architecture is designed to support the **Outbox Pattern** for higher resilience.

**How it works (The Pattern)**:
1.  Instead of sending to RabbitMQ directly, the service saves the event into a local `OUTBOX` table in the same transaction as the business data.
2.  A separate background process (Relay) reads from the `OUTBOX` table and pushes to RabbitMQ.

**Current Status**: As per the High-Level Design (HLD), this is a "candidate for implementation" to ensure that no notification is ever lost, even during a broker outage.

---

## 3. Resilience Pattern: Circuit Breaker

While not a transactional pattern like Saga, SkillSync uses **Resilience4j** to handle service failures.

- **Role**: If a downstream service (like `Mentor Service`) is failing, the Circuit Breaker "trips" and stops the `Auth Service` from trying again for a while.
- **Benefit**: This protects the `Auth Service` from getting overwhelmed by waiting for a service that isn't responding.

---

### Key Files & Concepts:
- **Choreography**: Low-level coordination where services react to events or direct calls without a central controller.
- **Compensating Transaction**: The "undo" logic (like `userRepository.delete(user)`) that runs when a step in a saga fails.
- [AuthService.java](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/auth-service/src/main/java/com/capgemini/auth/service/AuthService.java) - See the `register` method for the Saga implementation.
