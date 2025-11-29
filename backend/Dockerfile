# Multi-stage build для оптимизации размера образа
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем pom.xml и загружаем зависимости (кэшируется если pom не изменился)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Устанавливаем curl для healthcheck
RUN apk add --no-cache curl

# Создаем непривилегированного пользователя
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Копируем собранный JAR из стадии сборки
COPY --from=build /app/target/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Healthcheck для Docker (проверяем доступность Actuator health endpoint)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Запускаем приложение с профилем docker
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]

