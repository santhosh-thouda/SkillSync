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
2. Run `docker compose up --build`

This starts:

- PostgreSQL with separate service databases
- RabbitMQ with management UI
- Eureka server
- API gateway
- All Spring Boot microservices
