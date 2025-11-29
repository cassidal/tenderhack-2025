# CTE Grouping Service

Backend сервис для группировки товаров в Канонические Торговые Сущности (CTE/Карточки) с использованием LLM.

## Технологии

- Java 21
- Spring Boot 3.3+
- PostgreSQL
- Spring Data JPA (Hibernate)
- Lombok
- Jackson

## Архитектура

```
Controller -> Facade -> Service -> Repository -> Database
```

## Требования

- Java 21+
- PostgreSQL 14+
- Maven 3.8+

## Настройка базы данных

### С Docker Compose

База данных создается автоматически при первом запуске. Миграции Flyway выполняются автоматически при старте приложения.

### Локальная разработка

1. Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE cte_grouping;
```

2. Настройте подключение в `application.yml` или через переменные окружения.

3. Для миграций используйте Flyway (включен в Docker) или Hibernate ddl-auto (для локальной разработки).

## Запуск

### С Docker Compose (рекомендуется)

```bash
# Сборка и запуск всех сервисов (PostgreSQL + Backend)
docker-compose up -d

# Просмотр логов
docker-compose logs -f backend

# Остановка
docker-compose down
```

### Только база данных для локальной разработки

```bash
# Запуск только PostgreSQL
docker-compose -f docker-compose.dev.yml up -d

# Затем запускайте приложение локально через Maven
./mvnw spring-boot:run
```

### С Maven (локально)

```bash
./mvnw spring-boot:run
```

### Или соберите JAR

```bash
./mvnw clean package
java -jar target/cte-grouping-service-1.0.0-SNAPSHOT.jar
```

### Использование Makefile (опционально)

```bash
make build    # Собрать образы
make up       # Запустить сервисы
make logs     # Просмотр логов
make down     # Остановить сервисы
make clean    # Полная очистка (включая volumes)
make db-up    # Только база данных
```

## API Endpoints

| Method | Endpoint | Описание |
|--------|----------|----------|
| POST | `/api/grouping/request` | Создание задачи группировки |
| GET | `/api/grouping/{taskId}/results` | Получение результатов с пагинацией |
| GET | `/api/grouping/{taskId}/filters` | Получение доступных фильтров |
| POST | `/api/grouping/{taskId}/regenerate` | Перегенерация группировки |
| POST | `/api/grouping/{taskId}/approve` | Подтверждение группировки |
| POST | `/api/grouping/{taskId}/rate` | Оценка группировки |
| GET | `/api/cte/{id}` | Детальная информация о СТЕ |

## Примеры запросов

### Создание задачи группировки

```bash
curl -X POST http://localhost:8080/api/grouping/request \
  -H "Content-Type: application/json" \
  -d '{"query": "Сгруппируй сантехнику по бренду и материалу"}'
```

### Получение результатов

```bash
curl "http://localhost:8080/api/grouping/{taskId}/results?page=0&size=20"
```

### Получение деталей СТЕ

```bash
curl http://localhost:8080/api/cte/{cteId}
```

## Конфигурация LLM

В `application.yml`:

```yaml
llm:
  url: http://localhost:11434/api/generate
  model: qwen2.5:7b
```

## Разработка

Текущая реализация Facade (`GroupingFacadeStub`) возвращает stub-данные. 
Для реальной интеграции с LLM необходимо заменить на полноценную реализацию.

