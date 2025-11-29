package ru.tenderhack.cte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tenderhack.cte.dto.SchemaConfig;
import ru.tenderhack.cte.entity.*;
import ru.tenderhack.cte.exception.ResourceNotFoundException;
import ru.tenderhack.cte.mapper.CteMapper;
import ru.tenderhack.cte.repository.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Сервис для выполнения полного асинхронного pipeline группировки
 * Step A: Парсинг raw_products → task_processing_items
 * Step B: Кластеризация → Фильтрация → Генерация схем
 * Step C: Нормализация → Группировка → cte_entities
 * Step D: Финализация
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupingPipelineService {

    private final GroupingTaskRepository taskRepository;
    private final RawProductRepository rawProductRepository;
    private final TaskProcessingItemRepository taskProcessingItemRepository;
    private final CteRepository cteRepository;
    private final CharacteristicsParserService characteristicsParserService;
    private final PythonClusterClient pythonClusterClient;
    private final SignificanceFilterService significanceFilterService;
    private final OllamaService ollamaService;
    private final DynamicParserEngine parserEngine;
    private final CteMapper cteMapper;
    private final NotificationService notificationService;

    /**
     * Запускает полный pipeline обработки группировки асинхронно
     *
     * @param taskId ID задачи группировки
     * @param query текстовый запрос пользователя
     * @return CompletableFuture для отслеживания выполнения
     */
    @Async("groupingPipelineExecutor")
    public CompletableFuture<Void> runPipeline(UUID taskId, String query) {
        log.info("Starting grouping pipeline for task: {} with query: {}", taskId, query);

        try {
            // Обновляем статус на IN_PROGRESS
            updateTaskStatus(taskId, Status.IN_PROGRESS);

            // Step A: Fetch & Initial Parse
            log.info("Step A: Fetching and parsing raw products...");
            Set<String> allRawKeys = stepA_ParseRawProducts(taskId, query);

            // Step B: Intelligence (Clustering, Filtering, Schema Generation)
            log.info("Step B: Running intelligence layer...");
            StepBResult stepBResult = stepB_IntelligenceLayer(allRawKeys, taskId);

            // Step C: Normalization & Grouping
            log.info("Step C: Normalizing and grouping products...");
            stepC_NormalizeAndGroup(taskId, stepBResult.schemas, stepBResult.significantClusters);

            // Step D: Finalization
            log.info("Step D: Finalizing task...");
            updateTaskStatus(taskId, Status.COMPLETED);

            log.info("Pipeline completed successfully for task: {}", taskId);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Pipeline failed for task: {}", taskId, e);
            updateTaskStatus(taskId, Status.FAILED);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Step A: Получает raw_products, парсит характеристики и сохраняет в task_processing_items
     * Собирает все уникальные ключи для дальнейшей кластеризации
     */
    @Transactional
    protected Set<String> stepA_ParseRawProducts(UUID taskId, String query) {
        log.info("Fetching raw products for task: {}", taskId);

        // Получаем все raw_products (в будущем можно добавить фильтрацию по query)
        List<RawProductEntity> rawProducts = rawProductRepository.findAll();
        log.info("Found {} raw products to process", rawProducts.size());

        Set<String> allRawKeys = new HashSet<>();
        List<TaskProcessingItemEntity> processingItems = new ArrayList<>();

        for (RawProductEntity rawProduct : rawProducts) {
            // Парсим характеристики из текста в Map
            Map<String, String> characteristics = characteristicsParserService
                    .parseCharacteristics(rawProduct.getCharacteristics());

            // Собираем все уникальные ключи
            allRawKeys.addAll(characteristics.keySet());

            // Создаем обработанный элемент
            TaskProcessingItemEntity processingItem = TaskProcessingItemEntity.builder()
                    .taskId(taskId)
                    .rawProductId(rawProduct.getId())
                    .title(rawProduct.getTitle())
                    .imageUrl(rawProduct.getImageUrl())
                    .model(rawProduct.getModel())
                    .manufacturer(rawProduct.getManufacturer())
                    .country(rawProduct.getCountry())
                    .categoryName(rawProduct.getCategoryName())
                    .characteristics(characteristics)
                    .build();

            processingItems.add(processingItem);
        }

        // Сохраняем батчами
        int batchSize = 1000;
        for (int i = 0; i < processingItems.size(); i += batchSize) {
            int end = Math.min(i + batchSize, processingItems.size());
            List<TaskProcessingItemEntity> batch = processingItems.subList(i, end);
            taskProcessingItemRepository.saveAll(batch);
            log.debug("Saved batch {}-{} of {} items", i + 1, end, processingItems.size());
        }

        log.info("Step A completed: parsed {} products, collected {} unique keys", 
                processingItems.size(), allRawKeys.size());
        return allRawKeys;
    }

    /**
     * Step B: Интеллектуальный слой - кластеризация, фильтрация, генерация схем
     * @return мапа: первый ключ кластера -> SchemaConfig, и список значимых кластеров для маппинга
     */
    private StepBResult stepB_IntelligenceLayer(Set<String> allRawKeys, UUID taskId) {
        log.info("Step B: Processing {} unique keys", allRawKeys.size());

        // B1: Кластеризация через Python ML
        log.info("B1: Clustering keys via Python ML service...");
        List<String> keysList = new ArrayList<>(allRawKeys);
        List<List<String>> clusters = pythonClusterClient.getClusters(keysList);
        log.info("Received {} clusters from Python ML service", clusters.size());

        // B2: Получаем данные для фильтрации
        List<TaskProcessingItemEntity> processingItems = taskProcessingItemRepository.findByTaskId(taskId);
        List<Map<String, String>> rows = processingItems.stream()
                .map(item -> {
                    Map<String, String> row = new HashMap<>();
                    // Преобразуем characteristics Map в формат для фильтрации
                    row.putAll(item.getCharacteristics());
                    return row;
                })
                .toList();

        // B3: Фильтрация значимых кластеров
        log.info("B2: Filtering significant clusters...");
        List<List<String>> significantClusters = significanceFilterService
                .filterSignificantClusters(clusters, rows);
        log.info("{} clusters passed significance filter (from {})", 
                significantClusters.size(), clusters.size());

        // B4: Генерация схем через LLM
        log.info("B3: Generating schemas via LLM...");
        Map<String, SchemaConfig> schemas = ollamaService
                .generateSchemasForAllClusters(significantClusters);
        log.info("Generated {} schemas", schemas.size());

        return new StepBResult(schemas, significantClusters);
    }

    /**
     * Результат Step B - содержит схемы и кластеры для маппинга
     */
    private static class StepBResult {
        final Map<String, SchemaConfig> schemas;
        final List<List<String>> significantClusters;

        StepBResult(Map<String, SchemaConfig> schemas, List<List<String>> significantClusters) {
            this.schemas = schemas;
            this.significantClusters = significantClusters;
        }
    }

    /**
     * Step C: Нормализация значений и группировка продуктов в CTE
     */
    @Transactional
    protected void stepC_NormalizeAndGroup(UUID taskId, Map<String, SchemaConfig> schemas, List<List<String>> significantClusters) {
        log.info("Step C: Normalizing and grouping products...");

        // Получаем все обработанные элементы
        List<TaskProcessingItemEntity> processingItems = taskProcessingItemRepository.findByTaskId(taskId);
        log.info("Processing {} items for grouping", processingItems.size());

        // Создаем маппинг ключ -> unified_name из схем и кластеров
        Map<String, String> keyToUnifiedName = createKeyToUnifiedNameMapping(schemas, significantClusters);

        // Группируем продукты по нормализованным важным атрибутам
        Map<String, List<TaskProcessingItemEntity>> groupedProducts = new HashMap<>();

        for (TaskProcessingItemEntity item : processingItems) {
            // Нормализуем важные атрибуты
            Map<String, Object> normalizedImportant = normalizeAttributes(
                    item.getCharacteristics(), 
                    schemas, 
                    keyToUnifiedName,
                    true  // только важные
            );

            // Создаем ключ группировки из нормализованных важных атрибутов
            String groupKey = createGroupKey(normalizedImportant);

            groupedProducts.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(item);
        }

        log.info("Created {} groups from {} products", groupedProducts.size(), processingItems.size());

        // Создаем CTE для каждой группы
        List<CteEntity> cteEntities = new ArrayList<>();
        for (Map.Entry<String, List<TaskProcessingItemEntity>> entry : groupedProducts.entrySet()) {
            List<TaskProcessingItemEntity> group = entry.getValue();
            CteEntity cte = createCteFromGroup(group, schemas, keyToUnifiedName, taskId);
            cteEntities.add(cte);
        }

        // Сохраняем батчами
        int batchSize = 1000;
        for (int i = 0; i < cteEntities.size(); i += batchSize) {
            int end = Math.min(i + batchSize, cteEntities.size());
            List<CteEntity> batch = cteEntities.subList(i, end);
            cteRepository.saveAll(batch);
            log.debug("Saved CTE batch {}-{} of {}", i + 1, end, cteEntities.size());
        }

        log.info("Step C completed: created {} CTE entities", cteEntities.size());
    }

    /**
     * Создает маппинг исходных ключей на unified_name из схем и кластеров
     * Для каждого кластера все ключи маппятся на один unified_name
     */
    private Map<String, String> createKeyToUnifiedNameMapping(
            Map<String, SchemaConfig> schemas, 
            List<List<String>> significantClusters) {
        
        Map<String, String> mapping = new HashMap<>();

        // Проходим по кластерам
        for (List<String> cluster : significantClusters) {
            if (cluster.isEmpty()) continue;

            // Берем первый ключ кластера как correlation ID для поиска схемы
            String clusterFirstKey = cluster.get(0);
            SchemaConfig schema = schemas.get(clusterFirstKey);

            if (schema != null) {
                String unifiedName = schema.getUnifiedName();
                // Маппим все ключи кластера на один unified_name
                for (String key : cluster) {
                    mapping.put(key, unifiedName);
                }
            } else {
                log.warn("No schema found for cluster starting with: {}", clusterFirstKey);
            }
        }

        log.info("Created mapping for {} keys to {} unified names", 
                mapping.size(), new HashSet<>(mapping.values()).size());
        return mapping;
    }

    /**
     * Нормализует атрибуты используя схемы
     * @param importantOnly если true, нормализует только важные атрибуты (те, что есть в схемах)
     *                      если false, нормализует все атрибуты, которые есть в keyToUnifiedName
     */
    private Map<String, Object> normalizeAttributes(
            Map<String, String> rawCharacteristics,
            Map<String, SchemaConfig> schemas,
            Map<String, String> keyToUnifiedName,
            boolean importantOnly) {

        Map<String, Object> normalized = new HashMap<>();
        
        // Собираем множество важных unified_name (те, что есть в схемах)
        Set<String> importantUnifiedNames = schemas.values().stream()
                .map(SchemaConfig::getUnifiedName)
                .collect(java.util.stream.Collectors.toSet());

        for (Map.Entry<String, String> entry : rawCharacteristics.entrySet()) {
            String rawKey = entry.getKey();
            String rawValue = entry.getValue();

            if (rawValue == null || rawValue.trim().isEmpty() || rawValue.equalsIgnoreCase("NULL")) {
                continue; // Пропускаем пустые значения
            }

            String unifiedName = keyToUnifiedName.get(rawKey);
            if (unifiedName != null) {
                // Если importantOnly=true, пропускаем второстепенные
                if (importantOnly && !importantUnifiedNames.contains(unifiedName)) {
                    continue;
                }
                
                // Если importantOnly=false, пропускаем важные (они уже обработаны)
                if (!importantOnly && importantUnifiedNames.contains(unifiedName)) {
                    continue;
                }

                // Находим схему по unified_name
                SchemaConfig schema = schemas.values().stream()
                        .filter(s -> s.getUnifiedName().equals(unifiedName))
                        .findFirst()
                        .orElse(null);

                if (schema != null) {
                    // Парсим значение используя схему
                    Object parsedValue = parserEngine.parse(rawValue, schema);
                    normalized.put(unifiedName, parsedValue);
                } else {
                    // Если схемы нет, сохраняем как строку
                    normalized.put(unifiedName, rawValue);
                }
            }
        }

        return normalized;
    }

    /**
     * Создает ключ группировки из нормализованных атрибутов
     */
    private String createGroupKey(Map<String, Object> normalizedAttributes) {
        return normalizedAttributes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("|"));
    }

    /**
     * Создает CTE из группы продуктов
     */
    private CteEntity createCteFromGroup(
            List<TaskProcessingItemEntity> group,
            Map<String, SchemaConfig> schemas,
            Map<String, String> keyToUnifiedName,
            UUID taskId) {

        if (group.isEmpty()) {
            throw new IllegalArgumentException("Cannot create CTE from empty group");
        }

        // Берем первый продукт как базовый
        TaskProcessingItemEntity firstItem = group.get(0);

        // Нормализуем важные атрибуты (для группировки)
        // Важные атрибуты - это те, которые есть в схемах (использовались для группировки)
        Map<String, Object> importantNormalized = normalizeAttributes(
                firstItem.getCharacteristics(),
                schemas,
                keyToUnifiedName,
                true
        );

        // Преобразуем в AttributeJson для важных атрибутов
        List<AttributeJson> importantAttributes = importantNormalized.entrySet().stream()
                .map(e -> AttributeJson.builder()
                        .name(e.getKey())
                        .value(String.valueOf(e.getValue()))
                        .build())
                .toList();

        // Нормализуем второстепенные атрибуты (все остальные, которые есть в keyToUnifiedName, но не важные)
        // Второстепенные - это атрибуты, которые есть в keyToUnifiedName, но не использовались для группировки
        Map<String, Object> secondaryNormalized = normalizeAttributes(
                firstItem.getCharacteristics(),
                schemas,
                keyToUnifiedName,
                false
        );

        List<AttributeJson> secondaryAttributes = secondaryNormalized.entrySet().stream()
                .map(e -> AttributeJson.builder()
                        .name(e.getKey())
                        .value(String.valueOf(e.getValue()))
                        .build())
                .toList();

        // Собираем product_ids
        List<Long> productIds = group.stream()
                .map(TaskProcessingItemEntity::getRawProductId)
                .filter(Objects::nonNull)
                .toList();

        // Выбираем лучший image_url (первый не-null)
        String imageUrl = group.stream()
                .map(TaskProcessingItemEntity::getImageUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        return CteEntity.builder()
                .taskId(taskId)
                .imageUrl(imageUrl)
                .importantAttributes(importantAttributes)
                .secondaryAttributes(secondaryAttributes)
                .productIds(productIds)
                .build();
    }

    /**
     * Обновляет статус задачи
     */
    @Transactional
    protected void updateTaskStatus(UUID taskId, Status status) {
        GroupingTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        task.setStatus(status);
        taskRepository.save(task);
        notificationService.notifyTaskStatus(taskId, status, "Changed status of task " + taskId + " to " + status);
        log.info("Updated task {} status to {}", taskId, status);
    }
}

