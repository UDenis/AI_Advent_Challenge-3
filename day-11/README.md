## Проект:Пример простого MCP сервер напоминаний и агент к нему

Набор из двух модулей:
- `server` — MCP‑сервер на Ktor (SSE) с локальной БД SQLite (`server/tasks.db`) для хранения задач и уведомлений.
- `agent` — консольный агент, подключающийся к MCP‑серверу и использующий LLM DeepSeek.

Сервер предоставляет инструменты MCP:
- `add_task(content, deadLine)` — добавляет задачу и планирует уведомление за 30 минут до дедлайна.
- `get_tasks(day)` — возвращает задачи на указанный день (ISO‑дата `YYYY-MM-DD`).
- `date-time(expression)` — возвращает текущую дату/время в ISO8601. Поддерживает `today` и `tomorrow`.

Адрес SSE: `http://localhost:3001/`

### Требования
- JDK 17 (используется Gradle Wrapper)
- Docker (опционально) / Docker Compose (опционально)
- Для агента: переменная окружения `DEEPSEEK_API_KEY`

### Запуск сервера локально (Gradle)
```
./gradlew :server:run
```

Сервер стартует на `http://127.0.0.1:3001/sse`.

### Запуск сервера в Docker
Сборка и запуск из корня репозитория:
```
docker build -f server/Dockerfile -t mcp-server:latest .
docker run -d -p 3001:3001 --name mcp-server mcp-server:latest
```

Проверка: откройте `http://localhost:3001/sse`.

### Запуск сервера через Docker Compose
```
docker-compose up -d
```

В `docker-compose.yml` настроен healthcheck по `http://localhost:3001/sse`.

### Работа БД
- SQLite файл создаётся автоматически: `server/tasks.db`.
- Таблицы: `tasks` и `notifications` (уведомления отправляются за 30 минут до дедлайна или сразу, если дедлайн в прошлом/наступил).

### Запуск агента
1) Экспортируйте ключ:
```
export DEEPSEEK_API_KEY=your_key
```
2) Убедитесь, что сервер запущен на `http://localhost:3001`.
3) Запустите агент:
```
./gradlew :agent:run
```

Агент подключается по SSE к `http://localhost:3001` и демонстрационно выполняет два запроса: добавление встречи и получение списка дел на сегодня.

### Примеры использования MCP‑инструментов
- Добавление задачи:
```
{"tool":"add_task","arguments":{"content":"Встреча с клиентом","deadLine":"2025-10-30T12:30:00+03:00"}}
```
- Список задач на день:
```
{"tool":"get_tasks","arguments":{"day":"2025-10-30"}}
```
- Время:
```
{"tool":"date-time","arguments":{"expression":"today"}}
```

### Частые проблемы
- Нет соединения с сервером из агента: проверьте, что сервер запущен и доступен по `http://localhost:3001/sse`.
- Ошибка конфигурации агента: проверьте переменную `DEEPSEEK_API_KEY`.
- Очистка БД: остановите сервер и удалите файл `server/tasks.db`.

### Полезные команды
```
./gradlew :server:build
./gradlew :agent:build
./gradlew :server:run
./gradlew :agent:run
```
