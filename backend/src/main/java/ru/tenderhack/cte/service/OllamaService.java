package ru.tenderhack.cte.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.tenderhack.cte.config.OllamaConfigProperties;
import ru.tenderhack.cte.dto.SchemaConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для генерации схем атрибутов используя локальный LLM (Ollama)
 * Использует микро-батчинг для оптимизации контекстного окна
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaService {

    private final OllamaConfigProperties ollamaConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 5; // Размер микро-батча

    /**
     * Генерирует схемы для батча кластеров
     *
     * @param batchOfClusters список кластеров (обычно 5 групп синонимов)
     * @return мапа: первый ключ кластера -> SchemaConfig
     */
    public Map<String, SchemaConfig> generateSchemaForBatch(List<List<String>> batchOfClusters) {
        log.info("Generating schema for batch of {} clusters", batchOfClusters.size());

        try {
            // Формируем системный промпт
            String systemPrompt = buildSystemPrompt();
            
            // Формируем входные данные в формате JSON
            String inputJson = objectMapper.writeValueAsString(batchOfClusters);
            String fullPrompt = systemPrompt + "\n\nINPUT:\n" + inputJson + "\n\nOUTPUT:";

            // Вызываем Ollama
            String response = callOllama(fullPrompt);

            // Парсим ответ
            Map<String, SchemaConfig> schemas = parseResponse(response, batchOfClusters);

            log.info("Generated {} schemas from batch", schemas.size());
            return schemas;

        } catch (Exception e) {
            log.error("Error generating schema for batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate schema: " + e.getMessage(), e);
        }
    }

    /**
     * Генерирует схемы для всех кластеров с микро-батчингом
     *
     * @param allClusters все кластеры для обработки
     * @return мапа: первый ключ кластера -> SchemaConfig
     */
    public Map<String, SchemaConfig> generateSchemasForAllClusters(List<List<String>> allClusters) {
        log.info("Generating schemas for {} clusters using micro-batching", allClusters.size());

        Map<String, SchemaConfig> allSchemas = new HashMap<>();

        // Разбиваем на батчи по 5 кластеров
        for (int i = 0; i < allClusters.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, allClusters.size());
            List<List<String>> batch = allClusters.subList(i, end);

            log.info("Processing batch {}-{} of {}", i + 1, end, allClusters.size());

            Map<String, SchemaConfig> batchSchemas = generateSchemaForBatch(batch);
            allSchemas.putAll(batchSchemas);

            // Небольшая задержка между батчами для избежания перегрузки
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("Generated total {} schemas", allSchemas.size());
        return allSchemas;
    }

    private String buildSystemPrompt() {
        return """
            You are a Data Architect. You will receive a BATCH of attribute clusters.
            For EACH cluster, identify the semantic meaning.

            INPUT FORMAT: JSON List of Lists: [["Synonym1", "Synonym2"], ["SynonymA", "SynonymB"]]

            OUTPUT FORMAT: STRICT JSON Object.
            - Keys: MUST be the FIRST string from the input cluster (Correlation ID).
            - Values: Configuration Object.

            CONFIGURATION STRUCTURE:
            {
              "unified_name": "English snake_case key (e.g., 'width')",
              "display_name_ru": "Russian label for UI (e.g., 'Ширина')",
              "data_type": "Numeric" | "Boolean" | "String",
              "parsing_rule": {
                  "regex": "Python-style Regex with named groups (?<value>...)",
                  "base_unit": "mm/kg/etc (nullable)",
                  "unit_multipliers": {"cm": 10.0} (nullable)
              }
            }

            IMPORTANT:
            - Return ONLY valid JSON, no markdown, no explanations
            - For Numeric types, extract regex patterns like: (?<value>\\d+(?:\\.\\d+)?)\\s*(?<unit>mm|cm|kg|g)?
            - For Boolean types, use patterns like: (?<value>true|false|yes|no|да|нет)
            - For String types, use: (?<value>.*)
            """;
    }

    private String callOllama(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", ollamaConfig.model(),
                    "prompt", prompt,
                    "stream", false
            );

            String response = restTemplate.postForObject(
                    ollamaConfig.url(),
                    requestBody,
                    String.class
            );

            if (response == null) {
                throw new RuntimeException("Empty response from Ollama");
            }

            // Парсим ответ Ollama (может быть обернут в JSON)
            return extractJsonFromResponse(response);

        } catch (Exception e) {
            log.error("Error calling Ollama: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Ollama: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromResponse(String response) {
        try {
            // Пытаемся распарсить как JSON
            Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
            
            // Ollama возвращает поле "response" с текстом
            if (jsonResponse.containsKey("response")) {
                return (String) jsonResponse.get("response");
            }
            
            return response;
        } catch (Exception e) {
            // Если не JSON, возвращаем как есть
            return response;
        }
    }

    private Map<String, SchemaConfig> parseResponse(String response, List<List<String>> batchOfClusters) {
        try {
            // Очищаем ответ от markdown и лишнего текста
            String cleanedResponse = cleanJsonResponse(response);

            // Парсим JSON
            TypeReference<Map<String, SchemaConfig>> typeRef = new TypeReference<>() {};
            Map<String, SchemaConfig> schemas = objectMapper.readValue(cleanedResponse, typeRef);

            // Валидируем, что все кластеры обработаны
            for (List<String> cluster : batchOfClusters) {
                if (!cluster.isEmpty() && !schemas.containsKey(cluster.get(0))) {
                    log.warn("Missing schema for cluster: {}", cluster.get(0));
                }
            }

            return schemas;

        } catch (Exception e) {
            log.error("Error parsing Ollama response: {}", e.getMessage());
            log.debug("Response was: {}", response);
            throw new RuntimeException("Failed to parse schema response: " + e.getMessage(), e);
        }
    }

    private String cleanJsonResponse(String response) {
        // Убираем markdown код блоки
        String cleaned = response.replaceAll("```json", "").replaceAll("```", "");
        
        // Ищем JSON объект в тексте
        int startIdx = cleaned.indexOf('{');
        int endIdx = cleaned.lastIndexOf('}');
        
        if (startIdx >= 0 && endIdx > startIdx) {
            return cleaned.substring(startIdx, endIdx + 1);
        }
        
        return cleaned.trim();
    }
}

