# Используем образ с JDK 17 для сборки
FROM gradle:8.5.0-jdk17 AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Сначала копируем только файлы конфигурации Gradle для кэширования
COPY gradlew .
COPY gradle/ ./gradle/
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Копируем файлы модуля server
COPY server/ ./server/

# Копируем файл local.properties если существует
COPY local.properties* ./

# Устанавливаем права на выполнение и собираем проект
RUN chmod +x ./gradlew && \
    ./gradlew :server:build --no-daemon

# Используем минимальный образ с JRE для запуска
FROM eclipse-temurin:17-jre

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR из builder stage
COPY --from=builder /app/server/build/libs/*.jar app.jar

# Открываем порт для приложения
EXPOSE 3001

# Устанавливаем переменные окружения
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Запускаем приложение
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar 3001"]

