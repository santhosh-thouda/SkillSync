# SkillSync Docker Deployment Guide

This guide explains how to containerize a SkillSync microservice and push it to a Docker registry, from start to finish.

## 1. Prerequisites
- Docker Desktop installed and running.
- Maven installed (or use the Docker multi-stage build).
- A Docker Hub account (or a private registry like AWS ECR).

## 2. The Step-by-Step Process

### Step 1: Create a `Dockerfile`
Each service needs a `Dockerfile` in its root folder. SkillSync uses **Multi-Stage Builds** for efficiency.

**Example (`user-service/Dockerfile`):**
```dockerfile
# Stage 1: Build the JAR
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

### Step 2: Build the Docker Image
Navigate to the microservice directory and run the build command:
```bash
docker build -t skillsync-user-service .
```
- `-t`: Tags the image with a name.
- `.`: Uses the current directory as the build context.

### Step 3: Tag the Image for a Registry
To push an image to a registry (like Docker Hub), it must follow a specific naming convention: `username/repository:tag`.

```bash
docker tag skillsync-user-service yourusername/skillsync-user-service:latest
```

### Step 4: Log in to Docker Hub
```bash
docker login
```

### Step 5: Push the Image
```bash
docker push yourusername/skillsync-user-service:latest
```

---

## 3. Local Deployment with Docker Compose

While the steps above are for "pushing" to a registry, you can run everything locally without manual builds using **Docker Compose**.

### To start all services:
1. Ensure your `.env` file is configured with the correct database and RabbitMQ credentials.
2. Run the following command from the project root:
   ```bash
   docker-compose up --build -d
   ```
   - `--build`: Forces Docker to rebuild the images if code has changed.
   - `-d`: Runs containers in "detached" mode (background).

### To stop all services:
```bash
docker-compose down
```

## 4. Useful Docker Commands
- `docker ps`: View running containers.
- `docker logs -f <container_id>`: View live logs of a service.
- `docker exec -it <container_name> /bin/bash`: Enter a running container.
- `docker image ls`: List all local images.

---

### Key Files to Explore:
- [Dockerfile (user-service)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/user-service/Dockerfile)
- [docker-compose.yml (root)](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/docker-compose.yml)
- [.env.example](file:///c:/Users/santh/Advance_Java_MicroServices/SkillSync/.env.example) - Template for environment variables.
