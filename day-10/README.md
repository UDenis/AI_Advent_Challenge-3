## MCP Server (Open‑Meteo Weather)

### Запуск локально (Gradle)
```
./gradlew :server:run
```

Сервер поднимется на `http://localhost:3001/sse`.

### Запуск в Docker

Сборка и запуск образа выполняются из корня репозитория:
```
docker build -f server/Dockerfile -t mcp-server:latest .
docker run -d -p 3001:3001 --name mcp-server mcp-server:latest
```

Проверка: откройте `http://localhost:3001/sse`.

### Запуск через Docker Compose
```
docker-compose up -d
```


### Запуск агента
```
./gradlew :agent:run
```
