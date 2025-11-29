# Инструкция по импорту данных из CSV

## Установка зависимостей

### Linux/macOS

```bash
pip install -r requirements.txt
```

### Windows

На Windows могут возникнуть проблемы с установкой `psycopg2-binary`. Используйте:

```powershell
pip install -r requirements.txt --only-binary :all:
```

Или установите пакеты по отдельности:

```powershell
pip install pandas psycopg2-binary --only-binary :all:
```

Если не работает, попробуйте через conda:

```powershell
conda install -c conda-forge psycopg2
pip install pandas
```

## Использование

### Базовое использование

```bash
python import_csv.py table.csv
```

### С указанием описания задачи

```bash
python import_csv.py table.csv "Импорт шин из CSV файла"
```

### Настройка подключения к БД через переменные окружения

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=cte_grouping
export DB_USER=postgres
export DB_PASSWORD=postgres

python import_csv.py table.csv
```

Или для Windows PowerShell:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="cte_grouping"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"

python import_csv.py table.csv
```

### Для Docker

Если база данных запущена в Docker:

```bash
export DB_HOST=localhost
export DB_PORT=5432
python import_csv.py table.csv
```

## Что делает скрипт

1. **Читает CSV файл** с разделителем `;` (автоматически определяет кодировку)
2. **Создает таблицу `raw_products`** если её нет (через миграцию Flyway или автоматически)
3. **Загружает все данные из CSV** напрямую в таблицу `raw_products` со следующими колонками:
   - `id` - автоинкремент (BIGSERIAL)
   - `ste_id` - ID СТЕ из CSV (BIGINT)
   - `title` - название СТЕ (VARCHAR)
   - `image_url` - ссылка на картинку (VARCHAR)
   - `model` - модель (VARCHAR)
   - `country` - страна происхождения (VARCHAR)
   - `manufacturer` - производитель (VARCHAR)
   - `category_id` - ID категории (BIGINT)
   - `category_name` - название категории (VARCHAR)
   - `characteristics` - характеристики в текстовом формате (TEXT)
   - `created_at` - время создания записи (TIMESTAMP)

## Формат CSV

Ожидаемые колонки (все сохраняются в таблицу):
- `id сте` - ID товара (обязательно, BIGINT)
- `название сте` - название товара (VARCHAR)
- `ссылка на картинку сте` - URL изображения (VARCHAR)
- `модель` - модель товара (VARCHAR)
- `страна происхождения` - страна (VARCHAR)
- `производитель` - производитель (VARCHAR)
- `id категории` - ID категории (BIGINT)
- `название категории` - название категории (VARCHAR)
- `характеристики` - характеристики в формате "Ключ:Значение;Ключ:Значение" (TEXT)

## Примеры

### Локальная разработка

```bash
# Убедитесь, что PostgreSQL запущен
python import_csv.py table.csv
```

### С Docker

```bash
# Запустите только PostgreSQL
docker-compose -f docker-compose.dev.yml up -d

# Импортируйте данные
python import_csv.py table.csv
```

## Проверка результатов

После импорта данные будут в таблице `raw_products`. Проверьте:

```bash
# Через SQL
docker exec -it cte-postgres psql -U postgres -d cte_grouping -c "SELECT COUNT(*) FROM raw_products;"

# Просмотр первых записей
docker exec -it cte-postgres psql -U postgres -d cte_grouping -c "SELECT * FROM raw_products LIMIT 10;"

# Или подключитесь к БД напрямую
psql -h localhost -U postgres -d cte_grouping -c "SELECT * FROM raw_products LIMIT 10;"
```

## Troubleshooting

### Ошибка подключения к БД

Проверьте:
- Запущена ли база данных
- Правильность параметров подключения
- Доступность порта 5432

### Ошибка чтения файла (кодировка)

Скрипт автоматически пробует разные кодировки:
- UTF-8
- Windows-1251
- CP1251
- Latin-1

Если файл не читается, проверьте его кодировку вручную.

### Ошибка парсинга характеристик

Скрипт автоматически пропускает некорректные строки. Проверьте формат колонки "характеристики" - должно быть "Ключ:Значение;Ключ:Значение".

### Проблемы с кодировкой

Скрипт автоматически пробует разные кодировки. Если файл не читается, проверьте кодировку файла и при необходимости конвертируйте в UTF-8.

### NULL значения

Скрипт автоматически обрабатывает NULL значения и пустые строки. Поля с NULL или пустыми значениями будут сохранены как NULL в базе данных.

