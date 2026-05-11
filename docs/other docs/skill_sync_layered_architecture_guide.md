# SkillSync Layered Architecture Guide

This guide explains the architectural design of the SkillSync microservices. Each service follows a consistent, multi-layered approach to ensure a **Separation of Concerns (SoC)**, making the codebase easier to maintain, test, and scale.

## 1. The Core Layers

Each microservice is structured into the following primary layers:

### A. API/Controller Layer (`controller/`)
- **Responsibility**: Handles incoming HTTP requests and returns HTTP responses.
- **Key Functions**: Validates input `@Valid`, handles URL mapping `@GetMapping`, and delegates work to the Service layer.
- **Example**: `UserController`, `MentorController`.

### B. Service Layer (`service/`)
- **Responsibility**: Contains the core **Business Logic**.
- **Key Functions**: Orchestrates data flow, performs calculations, handles transactional boundaries, and communicates with other services or repositories.
- **Example**: `UserServiceImpl`, `MentorServiceImpl`.

### C. Data Access/Repository Layer (`repository/`)
- **Responsibility**: Handles all interactions with the database.
- **Key Functions**: Performs CRUD operations (Create, Read, Update, Delete) using Spring Data JPA.
- **Example**: `UserRepository`, `SkillRepository`.

### D. Domain/Entity Layer (`entity/`)
- **Responsibility**: Represents the database table structure as Java objects.
- **Key Functions**: Uses JPA annotations (`@Entity`, `@Table`, `@Id`) to map fields to database columns.
- **Example**: `User.java`, `Session.java`.

---

## 2. Supporting Layers (Data Transfer)

To keep our internal database structure (Entities) separate from our external API (what the client sees), we use:

- **DTO (Data Transfer Objects) Layer (`dto/`)**: Plain Java objects used only for carrying data between the API and the Client.
- **Mapper Layer (`mapper/`)**: Utility classes used to convert Entities into DTOs and vice-versa. This prevents exposing sensitive database fields (like passwords) to the frontend.

---

## 3. Cross-Cutting Layers

These layers provide shared functionality across the entire application:

- **`config/`**: Sets up beans for things like Security, RabbitMQ, and Swagger.
- **`exception/`**: Handles errors globally so the API always returns a predictable error format.
- **`security/`**: Manages JWT authentication and resource access control.

---

## 4. The Request Flow (The Journey of data)

When a user calls an API (e.g., `GET /users/1`), the data travels through the layers in this order:

1. **Client** (Browser/Postman) sends request to the **API Gateway**.
2. **Gateway** routes the request to the specific **Microservice** (e.g., User Service).
3. **Controller** receives the request and calls the **Service**.
4. **Service** performs business checks and calls the **Repository**.
5. **Repository** queries the **Database** and returns an **Entity**.
6. **Service** uses a **Mapper** to convert the **Entity** into a **DTO**.
7. **Controller** sends the **DTO** back to the **Client** as JSON.

---

### Key Package Structure Example:
```text
com.capgemini.user
├── config/       (Security, Swagger configs)
├── controller/   (API Endpoints)
├── dto/         (Data objects for API)
├── entity/       (Database models)
├── exception/    (Global error handling)
├── mapper/       (DTO <-> Entity conversion)
├── repository/   (Database access)
└── service/      (Business logic)
```
