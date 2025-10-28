FROM python:3.10-slim
COPY --from=docker.io/astral/uv:latest /uv /uvx /bin/

RUN uvx pip install mcp-server-calculator

# Запуск калькуляторного MCP сервера (по умолчанию через uvx)
CMD ["uvx", "mcp-server-calculator"]
