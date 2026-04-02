# SkillSync Dependency Injection Guide

This guide explains how **Dependency Injection (DI)**—a core pillar of the Spring Framework—is utilized in the SkillSync project to create a loosely coupled and maintainable microservices architecture.

## 1. What is Dependency Injection?
In simple terms, DI is a design pattern where a class does not create the objects it needs (its dependencies). Instead, those objects are "injected" into it by an external entity—in our case, the **Spring IoC (Inversion of Control) Container**.

## 2. Primary DI Pattern: Constructor Injection
The SkillSync project uses **Constructor Injection** as the primary way to provide dependencies. This is the industry-standard recommendation because:
- It makes dependencies **immutable** (using `final`).
- It ensures the class is never in an invalid state (always has its dependencies).
- It makes unit testing easier (you can pass mocks directly to the constructor).

### Example from `UserController.java`:
```java
@RestController
@RequiredArgsConstructor // Lombok generates the constructor for 'final' fields
public class UserController {
    // This dependency is injected via the constructor
    private final UserService userService; 
    
    // ... methods use userService
}
```

## 3. How Spring Finds Beans (Component Scanning)
Spring automatically detects classes that should be managed by its container based on **Stereotype Annotations**:

- **`@RestController`**: Used in the `controller` package to handle HTTP requests.
- **`@Service`**: Used in the `service` package to handle business logic.
- **`@Repository`**: (Implicitly provided by Spring Data JPA interfaces) handles database interactions.
- **`@Component`**: A generic marker for any Spring-managed bean (e.g., `JwtUtil`, `JwtAuthenticationFilter`).

## 4. Manual Bean Definitions (`@Bean`)
Sometimes we need to inject objects that we didn't write ourselves (like external library classes) or that need specific setup. We use `@Configuration` and `@Bean` for this.

### Example from `RestTemplateConfig.java`:
```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(); // Spring now manages this single 'RestTemplate' instance
    }
}
```
*Note: This bean can then be injected into any Service or Client using the same constructor pattern.*

## 5. Other DI Features in SkillSync

### A. Spring Data JPA Repositories
You don't write the implementation for `UserRepository`. You just define an interface, and Spring Data JPA **injects** a proxy implementation at runtime.

### B. Spring Cloud Feign Clients
For inter-service communication (e.g., `auth-service` calling `user-service`), we use interfaces annotated with `@FeignClient`. Spring Cloud generates the implementation and injects it where needed.

### C. Configuration Properties (`@Value`)
Spring also "injects" values from `application.properties` or environment variables into fields using the `@Value` annotation (e.g., JWT secrets, database URLs).

---

### Key Files to Explore:
- [UserController.java (user-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/user-service/src/main/java/com/capgemini/user/controller/UserController.java) - Example of Controller-Service injection.
- [UserServiceImpl.java (user-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/user-service/src/main/java/com/capgemini/user/service/UserServiceImpl.java) - Example of Service-Repository injection.
- [RestTemplateConfig.java (auth-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/auth-service/src/main/java/com/capgemini/auth/config/RestTemplateConfig.java) - Example of manual Bean definition.
