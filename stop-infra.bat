@echo off
echo Stopping Zipkin and RabbitMQ Docker containers...
docker compose stop zipkin rabbitmq
echo Done. Java services must be closed manually from their terminal windows.
pause
