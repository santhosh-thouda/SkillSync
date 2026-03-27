# SkillSync Deployment Notes

## Secrets

Application secrets are now externalized through environment variables in each `application.properties` file.
Use `.env.example` as the starting point for your local `.env`.

## Swagger / OpenAPI

Swagger UI is available for the REST services after startup:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`
- `http://localhost:8085/swagger-ui.html`
- `http://localhost:8086/swagger-ui.html`
- `http://localhost:8087/swagger-ui.html`
- `http://localhost:8080/swagger-ui.html`

## Docker

1. Copy `.env.example` to `.env`
2. Start Docker Desktop / Docker Engine
3. Run `docker compose up --build -d`
4. Check startup with `docker compose ps`
5. Stream logs with `docker compose logs -f api-gateway`

This starts:

- PostgreSQL with separate service databases
- RabbitMQ with management UI
- Eureka server
- API gateway
- All Spring Boot microservices

### Important Docker Note

The API gateway now uses Docker service names for the aggregated Swagger docs routes when running in containers.
Without that, `localhost` inside the gateway container points back to the gateway itself instead of the other services.

### Useful URLs

- API Gateway: `http://localhost:8080`
- Gateway Swagger UI: `http://localhost:8080/swagger-ui.html`
- Eureka Dashboard: `http://localhost:8761`
- RabbitMQ Management: `http://localhost:15672`
