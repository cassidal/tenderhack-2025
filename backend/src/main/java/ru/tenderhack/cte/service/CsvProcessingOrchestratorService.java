package ru.tenderhack.cte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tenderhack.cte.dto.SchemaConfig;

import java.io.File;
import java.util.*;

/**
 * Оркестрационный сервис для полного цикла обработки CSV
 * Объединяет все этапы: парсинг CSV -> кластеризация -> фильтрация -> генерация схем -> парсинг данных
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvProcessingOrchestratorService {

    private final PythonClusterClient pythonClusterClient;
    private final SignificanceFilterService significanceFilterService;
    private final OllamaService ollamaService;
    private final DynamicParserEngine parserEngine;
    private final CsvParserService csvParserService;

    /**
     * Полный цикл обработки CSV файла
     *
     * @param file CSV файл для обработки
     * @return результат обработки с схемами и распарсенными данными
     */
    public CsvProcessingResult processCsv(File file) {
        log.info("Starting CSV processing for file: {}", file.getName());

        try {
            // 1. Парсинг CSV
            log.info("Step 1: Parsing CSV file...");
            CsvParserService.CsvParseResult parseResult = csvParserService.parseCsv(file, ";");
            log.info("Parsed CSV: {} headers, {} rows", parseResult.getHeaders().size(), parseResult.getRows().size());

            // 2. Кластеризация через Python ML сервис
            log.info("Step 2: Clustering headers via Python ML service...");
            List<String> rawHeaders = parseResult.getHeaders();
            List<List<String>> clusters = pythonClusterClient.getClusters(rawHeaders);
            log.info("Clustering result: {} raw headers -> {} semantic clusters", 
                    rawHeaders.size(), clusters.size());

            // 3. Фильтрация значимых кластеров
            log.info("Step 3: Filtering significant clusters...");
            List<List<String>> significantClusters = significanceFilterService
                    .filterSignificantClusters(clusters, parseResult.getRows());
            log.info("Significance filter: {} clusters -> {} significant clusters", 
                    clusters.size(), significantClusters.size());

            // 4. Генерация схем через LLM
            log.info("Step 4: Generating schemas via LLM...");
            Map<String, SchemaConfig> schemas = ollamaService
                    .generateSchemasForAllClusters(significantClusters);
            log.info("Generated {} schemas from {} clusters", schemas.size(), significantClusters.size());

            // 5. Создание маппинга заголовок -> unified_name
            Map<String, String> headerToUnifiedName = createHeaderMapping(significantClusters, schemas);

            // 6. Парсинг всех строк данных
            log.info("Step 5: Parsing data rows...");
            List<Map<String, Object>> parsedRows = parseAllRows(
                    parseResult.getRows(), 
                    schemas, 
                    headerToUnifiedName
            );
            log.info("Parsed {} rows", parsedRows.size());

            return CsvProcessingResult.builder()
                    .originalHeaders(rawHeaders)
                    .clusters(clusters)
                    .significantClusters(significantClusters)
                    .schemas(schemas)
                    .headerToUnifiedName(headerToUnifiedName)
                    .parsedRows(parsedRows)
                    .build();

        } catch (Exception e) {
            log.error("Error processing CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Создает маппинг исходных заголовков на unified_name
     */
    private Map<String, String> createHeaderMapping(
            List<List<String>> clusters,
            Map<String, SchemaConfig> schemas) {

        Map<String, String> mapping = new HashMap<>();

        for (List<String> cluster : clusters) {
            if (cluster.isEmpty()) continue;

            // Берем первый ключ кластера как correlation ID
            String firstKey = cluster.get(0);
            SchemaConfig schema = schemas.get(firstKey);

            if (schema != null) {
                String unifiedName = schema.getUnifiedName();
                // Маппим все ключи кластера на один unified_name
                for (String key : cluster) {
                    mapping.put(key, unifiedName);
                }
            }
        }

        return mapping;
    }

    /**
     * Парсит все строки данных используя схемы
     */
    private List<Map<String, Object>> parseAllRows(
            List<Map<String, String>> rows,
            Map<String, SchemaConfig> schemas,
            Map<String, String> headerToUnifiedName) {

        return rows.stream()
                .map(row -> parseRow(row, schemas, headerToUnifiedName))
                .toList();
    }

    /**
     * Парсит одну строку данных
     */
    private Map<String, Object> parseRow(
            Map<String, String> row,
            Map<String, SchemaConfig> schemas,
            Map<String, String> headerToUnifiedName) {

        Map<String, Object> parsedRow = new HashMap<>();

        for (Map.Entry<String, String> entry : row.entrySet()) {
            String header = entry.getKey();
            String value = entry.getValue();

            String unifiedName = headerToUnifiedName.get(header);
            if (unifiedName != null) {
                SchemaConfig schema = schemas.get(unifiedName);
                if (schema != null) {
                    Object parsedValue = parserEngine.parse(value, schema);
                    parsedRow.put(unifiedName, parsedValue);
                } else {
                    // Если схемы нет, сохраняем как строку
                    parsedRow.put(unifiedName, value);
                }
            }
        }

        return parsedRow;
    }

    /**
     * Результат обработки CSV
     */
    @lombok.Data
    @lombok.Builder
    public static class CsvProcessingResult {
        private List<String> originalHeaders;
        private List<List<String>> clusters;
        private List<List<String>> significantClusters;
        private Map<String, SchemaConfig> schemas;
        private Map<String, String> headerToUnifiedName;
        private List<Map<String, Object>> parsedRows;
    }
}
