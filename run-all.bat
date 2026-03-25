@echo off

echo Starting Eureka Server...
start cmd /k "cd eureka-server && mvn spring-boot:run"

echo Waiting for Eureka Server to initialize...
timeout /t 15

echo Starting Backend Microservices...
start cmd /k "cd auth-service && mvn spring-boot:run"
start cmd /k "cd user-service && mvn spring-boot:run"
start cmd /k "cd mentor-service && mvn spring-boot:run"
start cmd /k "cd skill-service && mvn spring-boot:run"
start cmd /k "cd session-service && mvn spring-boot:run"
start cmd /k "cd group-service && mvn spring-boot:run"
start cmd /k "cd review-service && mvn spring-boot:run"
start cmd /k "cd notification-service && mvn spring-boot:run"

echo Waiting for Microservices to start...
timeout /t 20

echo Starting API Gateway...
start cmd /k "cd api-gateway && mvn spring-boot:run"

echo All SkillSync services started!
pause
