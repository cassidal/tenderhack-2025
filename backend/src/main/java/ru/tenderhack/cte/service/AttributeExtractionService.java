package ru.tenderhack.cte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tenderhack.cte.dto.SchemaConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Оркестрационный сервис для динамического извлечения атрибутов
 * Объединяет все компоненты: кластеризацию, фильтрацию, генерацию схем и парсинг
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeExtractionService {

    private final PythonClusterClient pythonClusterClient;
    private final SignificanceFilterService significanceFilterService;
    private final OllamaService ollamaService;
    private final DynamicParserEngine parserEngine;

    /**
     * Полный цикл извлечения атрибутов из CSV заголовков
     *
     * @param csvHeaders список заголовков CSV
     * @param sampleData мапа: заголовок -> пример значения (для фильтрации)
     * @param totalRows общее количество строк в CSV
     * @param clusteringThreshold порог для кластеризации (0.0 - 1.0)
     * @return мапа: unified_name -> SchemaConfig
     */
    public Map<String, SchemaConfig> extractAttributes(
            List<String> csvHeaders,
            Map<String, String> sampleData,
            int totalRows,
            double clusteringThreshold) {

        log.info("Starting attribute extraction for {} headers", csvHeaders.size());

        // Шаг 1: Кластеризация синонимов через Python ML сервис
        log.info("Step 1: Clustering headers...");
        List<List<String>> clusters = pythonClusterClient.getClusters(csvHeaders);
        log.info("Found {} clusters from {} headers", clusters.size(), csvHeaders.size());

        // Шаг 2: Фильтрация значимых кластеров
        log.info("Step 2: Filtering significant clusters...");
        List<List<String>> significantClusters = significanceFilterService
                .filterSignificantClusters(clusters, sampleData, totalRows);
        log.info("{} clusters passed significance filter", significantClusters.size());

        // Шаг 3: Генерация схем через LLM (микро-батчинг)
        log.info("Step 3: Generating schemas via LLM...");
        Map<String, SchemaConfig> schemas = ollamaService
                .generateSchemasForAllClusters(significantClusters);
        log.info("Generated {} schemas", schemas.size());

        return schemas;
    }

    /**
     * Парсит значение используя соответствующую схему
     *
     * @param rawValue исходное значение
     * @param schemaConfig конфигурация схемы
     * @return распарсенное значение
     */
    public Object parseValue(String rawValue, SchemaConfig schemaConfig) {
        return parserEngine.parse(rawValue, schemaConfig);
    }

    /**
     * Парсит строку данных используя все схемы
     *
     * @param dataRow мапа: заголовок -> значение
     * @param schemas мапа: unified_name -> SchemaConfig
     * @param headerToUnifiedName мапа: исходный заголовок -> unified_name
     * @return мапа: unified_name -> распарсенное значение
     */
    public Map<String, Object> parseDataRow(
            Map<String, String> dataRow,
            Map<String, SchemaConfig> schemas,
            Map<String, String> headerToUnifiedName) {

        Map<String, Object> parsedRow = new java.util.HashMap<>();

        for (Map.Entry<String, String> entry : dataRow.entrySet()) {
            String header = entry.getKey();
            String value = entry.getValue();

            String unifiedName = headerToUnifiedName.get(header);
            if (unifiedName != null) {
                SchemaConfig schema = schemas.get(unifiedName);
                if (schema != null) {
                    Object parsedValue = parseValue(value, schema);
                    parsedRow.put(unifiedName, parsedValue);
                }
            }
        }

        return parsedRow;
    }
}

