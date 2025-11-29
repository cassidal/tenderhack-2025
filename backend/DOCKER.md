# Docker Setup Guide

## Быстрый старт

### Запуск всего стека (PostgreSQL + Backend)

```bash
# Сборка и запуск
docker-compose up -d --build

# Просмотр логов
docker-compose logs -f backend

# Проверка статуса
docker-compose ps
```

### Остановка

```bash
docker-compose down
```

### Полная очистка (включая данные БД)

```bash
docker-compose down -v
```

## Структура

- **Dockerfile** - Multi-stage build для Spring Boot приложения
- **docker-compose.yml** - Полный стек (PostgreSQL + Backend)
- **docker-compose.dev.yml** - Только PostgreSQL для локальной разработки
- **db/migration/** - SQL миграции Flyway

## Переменные окружения

Можно переопределить через `.env` файл или переменные окружения:

```bash
# .env файл
LLM_URL=http://host.docker.internal:11434/api/generate
LLM_MODEL=qwen2.5:7b
SPRING_DATASOURCE_PASSWORD=your_password
```

## Миграции базы данных

Миграции Flyway выполняются автоматически при старте приложения в Docker.

Файлы миграций находятся в `src/main/resources/db/migration/`:
- `V1__Create_tables.sql` - Создание таблиц и индексов

## Health Checks

- **PostgreSQL**: Проверка через `pg_isready`
- **Backend**: Проверка через Actuator `/actuator/health`

## Полезные команды

```bash
# Пересборка после изменений
docker-compose up -d --build

# Просмотр логов только базы данных
docker-compose logs -f postgres

# Подключение к базе данных
docker exec -it cte-postgres psql -U postgres -d cte_grouping

# Выполнение SQL запроса
docker exec -it cte-postgres psql -U postgres -d cte_grouping -c "SELECT * FROM grouping_tasks;"

# Перезапуск только backend
docker-compose restart backend
```

## Разработка

Для локальной разработки используйте `docker-compose.dev.yml`:

```bash
# Запуск только PostgreSQL
docker-compose -f docker-compose.dev.yml up -d

# Запуск приложения локально через Maven
./mvnw spring-boot:run
```

## Troubleshooting

### Проблема: Backend не может подключиться к БД

Проверьте, что PostgreSQL готов:
```bash
docker-compose ps
# postgres должен быть в статусе "healthy"
```

### Проблема: Миграции не выполняются

Проверьте логи:
```bash
docker-compose logs backend | grep flyway
```

### Проблема: Порт 8080 занят

Измените порт в `docker-compose.yml`:
```yaml
ports:
  - "8081:8080"  # Внешний:Внутренний
```

