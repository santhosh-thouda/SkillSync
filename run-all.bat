@echo off

echo Starting Eureka Server...
start cmd /k "cd eureka-server && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""

echo Starting Zipkin Tracing (Docker)...
docker compose up -d zipkin

echo Waiting for Base Services to initialize...
timeout /t 10

echo Starting Backend Microservices...
start cmd /k "cd config-server && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""
timeout /t 25
start cmd /k "cd auth-service && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""
start cmd /k "cd user-service && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""
start cmd /k "cd mentor-service && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""
start cmd /k "cd skill-service && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""
start cmd /k "cd session-service && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""
start cmd /k "cd group-service && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""
start cmd /k "cd review-service && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""
start cmd /k "cd notification-service && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""

echo Waiting for Microservices to start...
timeout /t 15

echo Starting API Gateway...
start cmd /k "cd api-gateway && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx192m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC""

echo All SkillSync services started (with 192MB RAM limit)!
echo If services still fail, try increasing your Windows Paging File size.
pause
