# SkillSync Exception Handling Guide

This guide explains how exceptions are managed across the SkillSync microservices to ensure consistent and user-friendly error responses.

## 1. Overview
SkillSync uses a **Global Exception Handling** approach using Spring Boot's `@RestControllerAdvice`. This allows us to intercept exceptions thrown by any controller and transform them into a standardized JSON format.

## 2. The Standard Error Response
Every error returned by the API follows this structure:

```json
{
  "timestamp": "2026-03-27T13:20:45",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 1",
  "path": "/users/1",
  "validationErrors": { ... } // Only present for validation errors (400)
}
```

- **`timestamp`**: When the error occurred.
- **`status`**: The HTTP status code.
- **`error`**: The standard HTTP error description.
- **`message`**: A specific message explaining the cause.
- **`path`**: The API endpoint that was called.
- **`validationErrors`**: A map of fields and their specific validation messages (e.g., "email": "Invalid format").

## 3. Implementation Details

### A. Global Exception Handler (`GlobalExceptionHandler`)
Each microservice has a `GlobalExceptionHandler` class in its `exception` package.
- It is annotated with `@RestControllerAdvice`.
- It contains methods annotated with `@ExceptionHandler` for specific exception types.

### B. Common Exception Types Handled
1. **`ResourceNotFoundException`**: 
   - **Status**: 404 Not Found.
   - **Usage**: When an entity (User, Mentor, Skill, etc.) is not found in the database.
2. **`BadRequestException`**:
   - **Status**: 400 Bad Request.
   - **Usage**: For invalid business logic requests (e.g., trying to book a session in the past).
3. **`MethodArgumentNotValidException`**:
   - **Status**: 400 Bad Request.
   - **Usage**: Triggered automatically when `@Valid` bean validation fails on Request DTOs. It populates the `validationErrors` map.
4. **Generic `Exception`**:
   - **Status**: 500 Internal Server Error.
   - **Usage**: A fallback for any unexpected errors to avoid leaking internal stack traces.

## 4. How to Use in Code

### Throwing an Exception
When you need to signal an error in your Service or Controller, simply throw the appropriate custom exception:

```java
if (userRepository.findById(id).isEmpty()) {
    throw new ResourceNotFoundException("User not found with id: " + id);
}
```

### Adding a New Exception Handler
If you create a new custom exception, add a handler method to `GlobalExceptionHandler`:

```java
@ExceptionHandler(MyCustomException.class)
public ResponseEntity<Map<String, Object>> handleMyCustomException(MyCustomException ex, HttpServletRequest request) {
    return new ResponseEntity<>(buildErrorBody(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()), HttpStatus.CONFLICT);
}
```

---

### Key Files to Explore:
- [GlobalExceptionHandler.java (user-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/user-service/src/main/java/com/capgemini/user/exception/GlobalExceptionHandler.java)
- [ResourceNotFoundException.java (user-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/user-service/src/main/java/com/capgemini/user/exception/ResourceNotFoundException.java)
- [GlobalExceptionHandler.java (auth-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/auth-service/src/main/java/com/capgemini/auth/exception/GlobalExceptionHandler.java)
