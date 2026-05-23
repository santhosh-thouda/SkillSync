# SkillSync Project Dependencies

This document provides a comprehensive list of all dependencies used across the SkillSync project, categorized by Backend (Spring Boot Microservices) and Frontend (Angular). It explains the purpose of each dependency in the context of the application architecture.

## Backend Dependencies (Spring Boot & Spring Cloud)

### Core & Web
*   **`org.springframework.boot:spring-boot-starter-web`**: The core starter for building RESTful web applications using Spring MVC. It provides Tomcat as the default embedded container.
*   **`org.springframework.boot:spring-boot-starter-validation`**: Integrates Hibernate Validator for declarative bean validation using annotations (e.g., `@NotNull`, `@Email`).

### Data Persistence
*   **`org.springframework.boot:spring-boot-starter-data-jpa`**: Starter for using Spring Data JPA with Hibernate as the default JPA provider. Used for ORM and database operations.
*   **`org.postgresql:postgresql`**: The PostgreSQL JDBC driver, allowing the microservices to connect to the PostgreSQL databases.

### Security
*   **`org.springframework.boot:spring-boot-starter-security`**: Spring Security starter used for comprehensive authentication and authorization across the services.
*   **`io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`**: JJWT library used for generating, parsing, and validating JSON Web Tokens (JWT) for stateless authentication.

### Microservices Infrastructure (Spring Cloud)
*   **`org.springframework.cloud:spring-cloud-starter-netflix-eureka-server`**: Used exclusively in the `eureka-server` microservice to act as the Service Registry.
*   **`org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`**: Used by all other microservices to register themselves with the Eureka Server for dynamic discovery.
*   **`org.springframework.cloud:spring-cloud-starter-gateway`**: Used in the `api-gateway` service to route incoming requests to the appropriate backend microservices and handle cross-cutting concerns (like CORS).
*   **`org.springframework.cloud:spring-cloud-config-server`**: Used in the `config-server` to serve centralized configuration files from a repository.
*   **`org.springframework.cloud:spring-cloud-starter-config`**: Used by microservices to fetch their specific configurations from the Config Server on startup.
*   **`org.springframework.cloud:spring-cloud-starter-bootstrap`**: Enables the legacy bootstrap context for loading configuration properties (like Config Server URIs) early in the application lifecycle.
*   **`org.springframework.cloud:spring-cloud-starter-openfeign`**: Provides a declarative REST client to simplify inter-service communication (e.g., a service calling another service).
*   **`org.springframework.cloud:spring-cloud-starter-loadbalancer`**: Client-side load balancer used in conjunction with Feign and Eureka to distribute requests among multiple instances of a service.
*   **`org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j`**: Implements the Circuit Breaker pattern to handle failures gracefully and prevent cascading failures across microservices.

### Messaging & Real-Time Communication
*   **`org.springframework.boot:spring-boot-starter-amqp`**: Advanced Message Queuing Protocol starter, used for integrating with RabbitMQ for asynchronous event-driven communication (e.g., sending notification events).
*   **`org.springframework.boot:spring-boot-starter-websocket`**: Used for building WebSocket applications, specifically for the real-time Group Chat feature using STOMP.

### Utility & Documentation
*   **`org.projectlombok:lombok`**: A Java compilation tool used to reduce boilerplate code by auto-generating getters, setters, constructors, builders, and loggers.
*   **`org.springframework.boot:spring-boot-starter-mail`**: Used in the `auth-service` (or `notification-service`) to send emails via SMTP (e.g., OTP verification).
*   **`org.springdoc:springdoc-openapi-starter-webmvc-ui`**: Automatically generates OpenAPI 3 documentation and provides the Swagger UI interface for testing REST APIs.
*   **`org.springdoc:springdoc-openapi-starter-webflux-ui`**: Swagger UI generator specifically for reactive web applications (used in the API Gateway).

### Observability & Operations
*   **`org.springframework.boot:spring-boot-starter-actuator`**: Provides production-ready endpoints (like `/health`, `/metrics`) to monitor and manage the applications.
*   **`io.micrometer:micrometer-tracing-bridge-brave`**: Micrometer Tracing bridge to the Brave tracer, used for distributed tracing across microservices.
*   **`io.zipkin.reporter2:zipkin-reporter-brave`**: Exports the distributed tracing data to a Zipkin server to visualize the flow of requests.
*   **`org.springframework.boot:spring-boot-devtools`**: Provides developer conveniences like automatic application restarts and LiveReload during development.

### Testing
*   **`org.springframework.boot:spring-boot-starter-test`**: Core Spring Boot testing starter (includes JUnit, Mockito, AssertJ, Spring Test).
*   **`org.springframework.security:spring-security-test`**: Utilities for testing Spring Security features.
*   **`org.springframework.amqp:spring-rabbit-test`**: Utilities for testing RabbitMQ listeners and publishers.
*   **`org.mockito:mockito-junit-jupiter`**: Mockito extension for JUnit 5 integration.

---

## Frontend Dependencies (Angular & Node)

### Core Angular
*   **`@angular/core`, `@angular/common`, `@angular/compiler`, `@angular/platform-browser`**: The foundational libraries of the Angular framework required to run the application in the browser.
*   **`@angular/router`**: The routing library that enables navigation between different views/components in the Single Page Application (SPA).
*   **`@angular/forms`**: Provides support for building both Template-driven and Reactive forms with built-in validation.
*   **`@angular/animations`**: Enables the creation of rich, complex animations for Angular components.

### UI Components & Styling
*   **`@angular/material`**: The official Angular component library that implements Google's Material Design principles (used for buttons, dialogs, inputs, etc.).
*   **`@angular/cdk`**: The Component Dev Kit provides accessible and high-performance behaviors (like overlays, drag-and-drop, virtual scrolling) for building custom UI components.
*   **`tailwindcss`**: A utility-first CSS framework used for rapidly styling custom components directly via HTML classes.
*   **`postcss`, `autoprefixer`**: Post-processing tools used alongside TailwindCSS to parse CSS and automatically add vendor prefixes for browser compatibility.
*   **`@tailwindcss/forms`**: A plugin that provides a basic reset for form styles, making them easier to style with Tailwind utilities.

### State Management & Reactivity
*   **`@ngrx/signals`**: Modern, reactive state management library leveraging Angular's new Signal API for fine-grained reactivity.
*   **`rxjs`**: Reactive Extensions for JavaScript, used heavily by Angular for handling asynchronous operations, events, and HTTP requests via Observables.
*   **`zone.js`**: An execution context for JavaScript that helps Angular know when to run change detection and update the UI.

### WebSockets & Real-time
*   **`@stomp/stompjs`**: A STOMP client library for JavaScript used to communicate with the Spring Boot backend over WebSockets (used for the real-time chat).
*   **`sockjs-client`**: Provides a WebSocket-like object for browsers that don't support WebSockets natively, acting as a fallback mechanism for STOMP.

### Development & Build Tools
*   **`@angular/cli`**: The command-line interface tool that initializes, develops, scaffolds, and maintains Angular applications.
*   **`@angular/build`, `@angular/compiler-cli`**: Tools used by the CLI to compile Angular components and TypeScript into browser-readable JavaScript.
*   **`typescript`**: A typed superset of JavaScript that compiles to plain JavaScript, used as the primary language for the frontend.
*   **`vitest`**: A blazing fast unit test framework powered by Vite, used for running frontend unit tests.
*   **`prettier`**: An opinionated code formatter used to enforce a consistent code style across the frontend codebase.
*   **`tslib`**: A runtime library that contains TypeScript helper functions to reduce the size of compiled code.
