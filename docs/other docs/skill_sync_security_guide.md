# SkillSync Security Guide: Spring Security & JWT

This guide explains how security is implemented in the SkillSync microservices architecture using Spring Security and JSON Web Tokens (JWT).

## 1. What is Spring Security?
**Spring Security** is a powerful and highly customizable authentication and access-control framework for Java applications. It is the de-facto standard for securing Spring-based applications.

In SkillSync, it handles:
- **Authentication**: Verifying "who you are" (Login).
- **Authorization**: Verifying "what you are allowed to do" (Roles like `ADMIN`, `MENTOR`, `LEARNER`).
- **Protection**: Handling CORS, CSRF, and securing internal API endpoints.

## 2. What is JWT (JSON Web Token)?
**JWT** is an open standard (RFC 7519) that defines a compact and self-contained way for securely transmitting information between parties as a JSON object.

- **Compact**: Can be sent via URL, POST parameter, or inside an HTTP header.
- **Self-contained**: The payload contains all the required information about the user, avoiding the need to query the database more than once.
- **Signed**: Tokens are digitally signed using a secret key, so they cannot be tampered with.

## 3. How it Works (The Concept)
Instead of the server keeping a "Session" (stateful), it gives the client a "Token" (stateless).
1. **Login**: User provides email/password.
2. **Token Issuance**: Server verifies credentials and signs a JWT with a secret key.
3. **Storage**: Client stores the token (e.g., in LocalStorage or a Cookie).
4. **Usage**: For every request, the client sends the token in the `Authorization` header.
5. **Verification**: The server checks the signature. If valid, it trusts the identity in the token.

## 4. Implementation in SkillSync

### A. Auth Service (`auth-service`)
This is the "Identity Provider".
- **`SecurityConfig`**: Defines the security rules for the auth service itself.
- **`JwtUtil`**: The core utility class that:
    - Generates tokens (with `userId`, `email`, and `role`).
    - Validates tokens.
    - Extracts information (claims) from tokens.
- **`CustomUserDetailsService`**: Loads user data from the database to verify passwords during login.

### B. Other Microservices (e.g., `user-service`, `mentor-service`)
These are "Resource Servers".
- **`JwtAuthenticationFilter`**: An interceptor that runs before every request. It looks for the `Authorization: Bearer <token>` header.
- **`JwtTokenService`**: A service used by the filter to parse and validate the token.
- **`SecurityConfig`**: Ensures that all endpoints (except Swagger documentation) require a valid authentication token.

### C. API Gateway (`api-gateway`)
- Acts as the entry point.
- Currently, it permits all traffic through, but in a production-ready setup, it can also be configured to validate tokens before routing them to internal services.

## 5. The Security Flow

### Phase 1: Authentication (Login)
1. **Request**: Client sends `POST /auth/login` with email/password to the Gateway.
2. **Routing**: Gateway sends it to `auth-service`.
3. **Validation**: `auth-service` checks the database.
4. **Generation**: If correct, `JwtUtil` creates a JWT.
5. **Response**: Client receives the JWT.

### Phase 2: Authorized Request (Accessing Data)
1. **Request**: Client sends `GET /users/profile` with `Authorization: Bearer <TOKEN>`.
2. **Routing**: Gateway routes to `user-service`.
3. **Interception**: `JwtAuthenticationFilter` in `user-service` catches the request.
4. **Verification**: It uses `JwtTokenService` to check if the token is valid and hasn't expired.
5. **Context**: If valid, it tells Spring Security: "This is User X with Role Y".
6. **Execution**: The Controller method runs and returns the profile data.

---

### Key Files to Explore:
- [JwtUtil.java (auth-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/auth-service/src/main/java/com/capgemini/auth/security/JwtUtil.java) - Where tokens are created.
- [SecurityConfig.java (auth-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/auth-service/src/main/java/com/capgemini/auth/config/SecurityConfig.java) - Security rules for authentication.
- [JwtAuthenticationFilter.java (user-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/user-service/src/main/java/com/capgemini/user/security/JwtAuthenticationFilter.java) - How other services verify you.
