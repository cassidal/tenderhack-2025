# Dynamic Attribute Extraction System

Гибридная система для динамического извлечения и парсинга атрибутов из CSV файлов.

## Архитектура

### 1. Python ML Service (FastAPI)
- **Назначение**: Семантическая кластеризация синонимов заголовков CSV
- **Технологии**: FastAPI, sentence-transformers (rubert-tiny2), scikit-learn
- **Порт**: 8000

### 2. Java Backend (Spring Boot)
- **Назначение**: Основная логика - фильтрация, генерация схем через LLM, парсинг
- **Технологии**: Spring Boot 3.x, Lombok, Jackson, RestTemplate
- **Порт**: 8080

## Установка и запуск

### Python ML Service

```bash
cd ml-service
pip install -r requirements.txt
python main.py
```

Сервис будет доступен на `http://localhost:8000`

### Java Backend

```bash
cd backend
./mvnw spring-boot:run
```

Сервис будет доступен на `http://localhost:8080`

### Требования

- Python 3.8+
- Java 21+
- Ollama с моделью `qwen2.5:7b` (запущен локально)

## Компоненты системы

### 1. PythonClientService
Вызывает Python ML сервис для кластеризации синонимов.

**Пример:**
```java
List<List<String>> clusters = pythonClientService.clusterKeys(
    Arrays.asList("Width", "Weight", "Width (mm)", "Mass"), 
    0.4
);
// Результат: [["Width", "Width (mm)"], ["Weight", "Mass"]]
```

### 2. SignificanceFilterService
Фильтрует бесполезные кластеры перед вызовом LLM.

**Критерии фильтрации:**
- Coverage < 10% - атрибут слишком редкий
- Cardinality > 90% - вероятно ID/серийный номер
- Cardinality == 1 - константное значение

### 3. OllamaService
Генерирует схемы атрибутов через локальный LLM.

**Особенности:**
- Микро-батчинг (по 5 кластеров)
- Оптимизация контекстного окна
- Автоматическая очистка ответов от markdown

### 4. DynamicParserEngine
Парсит значения на основе динамически сгенерированных схем.

**Поддерживаемые типы:**
- **Numeric**: с единицами измерения и конвертацией
- **Boolean**: русские и английские варианты
- **String**: текстовые значения

## Пример использования

```java
@Autowired
private AttributeExtractionService extractionService;

// 1. Извлечение атрибутов
Map<String, SchemaConfig> schemas = extractionService.extractAttributes(
    csvHeaders,
    sampleData,
    totalRows,
    0.4  // clustering threshold
);

// 2. Парсинг значения
Object parsedValue = extractionService.parseValue("256 мм", schemaConfig);

// 3. Парсинг всей строки
Map<String, Object> parsedRow = extractionService.parseDataRow(
    dataRow,
    schemas,
    headerToUnifiedName
);
```

## Формат SchemaConfig

```json
{
  "unified_name": "width",
  "display_name_ru": "Ширина",
  "data_type": "Numeric",
  "parsing_rule": {
    "regex": "(?<value>\\d+(?:\\.\\d+)?)\\s*(?<unit>mm|cm)?",
    "base_unit": "mm",
    "unit_multipliers": {
      "cm": 10.0
    }
  }
}
```

## Конфигурация

### application.yml

```yaml
ml:
  service:
    url: http://localhost:8000

ollama:
  url: http://localhost:11434/api/generate
  model: qwen2.5:7b
```

## API Endpoints

### Python ML Service

- `POST /cluster_keys` - кластеризация ключей
- `GET /health` - проверка здоровья сервиса

### Java Backend

Все существующие endpoints остаются без изменений. Новые сервисы доступны через dependency injection.

## Производительность

- **Кластеризация**: ~100-500 мс для 100 заголовков
- **Фильтрация**: <10 мс
- **Генерация схем**: ~2-5 сек на батч из 5 кластеров
- **Парсинг**: <1 мс на значение

## Troubleshooting

### ML Service не доступен
Проверьте, что Python сервис запущен:
```bash
curl http://localhost:8000/health
```

### Ollama не отвечает
Убедитесь, что Ollama запущен и модель загружена:
```bash
ollama list
ollama pull qwen2.5:7b
```

### Ошибки парсинга
Проверьте формат regex в `parsing_rule`. Используйте Python-style regex с именованными группами.

