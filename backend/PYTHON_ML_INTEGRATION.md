# Python ML Integration Guide

## Обзор

Интеграция Python ML сервиса для семантической кластеризации заголовков CSV перед обработкой через LLM.

## Архитектура потока данных

```
CSV Headers (350)
  ↓
PythonClusterClient.getClusters()
  ↓
Python ML Service (rubert-tiny2)
  ↓
Clusters (~40 semantic groups)
  ↓
SignificanceFilterService.filterSignificantClusters()
  ↓
Significant Clusters (~20)
  ↓
OllamaService.generateSchemasForAllClusters()
  ↓
SchemaConfig Map
  ↓
DynamicParserEngine.parse()
  ↓
Parsed Data
```

## Компоненты

### 1. PythonClusterClient

**Назначение**: REST клиент для Python ML сервиса

**Метод**: `getClusters(List<String> rawHeaders)`

**Особенности**:
- Fallback логика: если Python сервис недоступен, возвращает каждый заголовок как отдельный кластер
- Обработка `ConnectException` для graceful degradation
- Логирование количества кластеров vs исходных заголовков

**Конфигурация**:
```yaml
ml:
  service:
    url: http://localhost:8000
    clustering:
      threshold: 0.4
```

### 2. SignificanceFilterService (обновлен)

**Новый метод**: `filterSignificantClusters(List<List<String>> clusters, List<Map<String, String>> rows)`

**Логика**:
- Обрабатывает кластер как единый атрибут
- Для каждой строки берет первое не-null значение из ключей кластера ("Effective Coverage")
- Применяет те же правила: Coverage > 10%, Cardinality < 90%

**Пример**:
```java
// Кластер: ["Weight", "Mass", "Вес"]
// Для строки: {"Weight": "50kg", "Mass": null, "Вес": null}
// Берется "50kg" как эффективное значение
```

### 3. CsvProcessingOrchestratorService

**Назначение**: Полный workflow обработки CSV

**Метод**: `processCsv(File file)`

**Этапы**:
1. Парсинг CSV → заголовки и строки
2. Кластеризация → Python ML сервис
3. Фильтрация → SignificanceFilterService
4. Генерация схем → OllamaService
5. Парсинг данных → DynamicParserEngine

## Использование

### Базовый пример

```java
@Autowired
private CsvProcessingOrchestratorService orchestrator;

File csvFile = new File("table.csv");
CsvProcessingOrchestratorService.CsvProcessingResult result = 
    orchestrator.processCsv(csvFile);

// Результат содержит:
// - originalHeaders: исходные заголовки
// - clusters: кластеры от Python
// - significantClusters: отфильтрованные кластеры
// - schemas: сгенерированные схемы
// - parsedRows: распарсенные данные
```

### Только кластеризация

```java
@Autowired
private PythonClusterClient pythonClusterClient;

List<String> headers = Arrays.asList("Width", "Weight", "Width (mm)", "Mass");
List<List<String>> clusters = pythonClusterClient.getClusters(headers);
// Результат: [["Width", "Width (mm)"], ["Weight", "Mass"]]
```

### Только фильтрация

```java
@Autowired
private SignificanceFilterService filterService;

List<List<String>> clusters = ...; // от Python
List<Map<String, String>> rows = ...; // из CSV

List<List<String>> significant = filterService.filterSignificantClusters(clusters, rows);
```

## Fallback поведение

Если Python ML сервис недоступен:

1. `PythonClusterClient` автоматически создает fallback кластеры
2. Каждый заголовок становится отдельным кластером
3. Pipeline продолжает работу без семантической кластеризации
4. В логах появляется предупреждение

**Логи**:
```
WARN: Python ML service is not available (Connection refused). 
      Falling back to single-element clusters. 
      Pipeline will continue but without semantic clustering.
```

## Производительность

- **Кластеризация**: ~100-500 мс для 350 заголовков
- **Фильтрация**: ~10-50 мс для 40 кластеров
- **Fallback**: <1 мс (просто создание списков)

## Troubleshooting

### Python сервис не отвечает

Проверьте:
```bash
curl http://localhost:8000/health
```

Если недоступен, система автоматически переключится на fallback режим.

### Пустые кластеры

Если `getClusters()` возвращает пустой список, проверьте:
- Формат ответа от Python сервиса
- Логи Python сервиса
- Порог кластеризации (может быть слишком строгим)

### Ошибки фильтрации

Если все кластеры отфильтровываются:
- Проверьте данные в CSV (может быть слишком мало строк)
- Уменьшите `MIN_COVERAGE` или увеличьте `MAX_CARDINALITY` в `SignificanceFilterService`

