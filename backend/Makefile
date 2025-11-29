.PHONY: build up down logs restart clean db-up db-down

# Сборка и запуск всех сервисов
build:
	docker-compose build

up:
	docker-compose up -d

down:
	docker-compose down

logs:
	docker-compose logs -f backend

restart:
	docker-compose restart backend

# Очистка (включая volumes)
clean:
	docker-compose down -v
	docker system prune -f

# Только база данных для разработки
db-up:
	docker-compose -f docker-compose.dev.yml up -d

db-down:
	docker-compose -f docker-compose.dev.yml down

# Пересборка и запуск
rebuild:
	docker-compose up -d --build

# Просмотр логов базы данных
db-logs:
	docker-compose logs -f postgres

