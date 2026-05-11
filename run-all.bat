@echo off
setlocal

echo ============================================================
echo   SkillSync - Local Dev Startup
echo   Infrastructure : Docker  (Zipkin + RabbitMQ)
echo   Java Services  : Local   (mvn spring-boot:run)
echo ============================================================
echo.

:: ── 1. Start infrastructure containers ──────────────────────
echo [1/6] Starting Zipkin and RabbitMQ in Docker...
docker compose up -d zipkin rabbitmq

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Docker Compose failed. Make sure Docker Desktop is running.
    pause
    exit /b 1
)

echo.
echo [2/6] Waiting for Zipkin to be ready (port 9411)...
:wait_zipkin
timeout /t 3 /nobreak >nul
curl -s http://localhost:9411/health >nul 2>&1
if %ERRORLEVEL% NEQ 0 goto wait_zipkin
echo       Zipkin is UP  ^>  http://localhost:9411

echo.
echo [3/6] Waiting for RabbitMQ to be ready...
:wait_rabbitmq
timeout /t 3 /nobreak >nul
docker exec skillsync-rabbitmq rabbitmq-diagnostics -q ping >nul 2>&1
if %ERRORLEVEL% NEQ 0 goto wait_rabbitmq
echo       RabbitMQ is UP  ^>  http://localhost:15672  (guest / guest)

:: ── 2. Start Eureka first ────────────────────────────────────
echo.
echo [4/6] Starting Eureka Server (local)...
start "Eureka Server" cmd /k "cd eureka-server && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"

echo       Waiting 20s for Eureka to register...
timeout /t 20 /nobreak >nul

:: ── 3. Start Config Server ───────────────────────────────────
echo.
echo [5/6] Starting Config Server (local)...
start "Config Server" cmd /k "cd config-server && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"

echo       Waiting 25s for Config Server to be ready...
timeout /t 25 /nobreak >nul

:: ── 4. Start all microservices ───────────────────────────────
echo.
echo [6/6] Starting all microservices (local)...

start "Auth Service"         cmd /k "cd auth-service         && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"
start "User Service"         cmd /k "cd user-service         && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"
start "Mentor Service"       cmd /k "cd mentor-service       && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"
start "Skill Service"        cmd /k "cd skill-service        && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"
start "Session Service"      cmd /k "cd session-service      && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"
start "Group Service"        cmd /k "cd group-service        && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"
start "Review Service"       cmd /k "cd review-service       && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"
start "Notification Service" cmd /k "cd notification-service && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"

echo       Waiting 20s for microservices to register with Eureka...
timeout /t 20 /nobreak >nul

:: ── 5. Start API Gateway last ────────────────────────────────
start "API Gateway" cmd /k "cd api-gateway && set MAVEN_OPTS=-Xmx192m -Xms64m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC && mvn spring-boot:run"

echo.
echo ============================================================
echo   All services started!
echo.
echo   Zipkin        : http://localhost:9411
echo   RabbitMQ UI   : http://localhost:15672  (guest / guest)
echo   Eureka        : http://localhost:8761
echo   API Gateway   : http://localhost:8080
echo   Frontend      : cd frontend ^&^& npm start
echo ============================================================
echo.
pause
