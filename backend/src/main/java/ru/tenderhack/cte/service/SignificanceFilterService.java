package ru.tenderhack.cte.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис фильтрации значимости атрибутов ("Significance Funnel")
 * Фильтрует бесполезные кластеры ПЕРЕД вызовом LLM для экономии ресурсов
 */
@Slf4j
@Service
public class SignificanceFilterService {

    private static final double MIN_COVERAGE = 0.1; // Минимум 10% строк должны содержать атрибут
    private static final double MAX_CARDINALITY = 0.9; // Максимум 90% уникальности (иначе это ID)

    /**
     * Проверяет, является ли кластер значимым для группировки
     *
     * @param clusterKeys список ключей в кластере
     * @param sampleValues мапа: ключ -> значение из выборки данных
     * @param totalRows общее количество строк в данных
     * @return true если кластер значим, false если его можно отфильтровать
     */
    public boolean isSignificant(List<String> clusterKeys, Map<String, String> sampleValues, int totalRows) {
        if (clusterKeys == null || clusterKeys.isEmpty() || totalRows == 0) {
            return false;
        }

        // 1. Coverage: проверяем, существует ли атрибут хотя бы в 10% строк
        long rowsWithAttribute = sampleValues.entrySet().stream()
                .filter(entry -> clusterKeys.contains(entry.getKey()))
                .filter(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty())
                .count();

        double coverage = (double) rowsWithAttribute / totalRows;
        if (coverage < MIN_COVERAGE) {
            log.debug("Cluster {} filtered: coverage {} < {}", clusterKeys, coverage, MIN_COVERAGE);
            return false; // Слишком редкий атрибут
        }

        // 2. Cardinality: проверяем уникальность значений
        Set<String> uniqueValues = sampleValues.entrySet().stream()
                .filter(entry -> clusterKeys.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .filter(value -> value != null && !value.trim().isEmpty())
                .collect(Collectors.toSet());

        int uniqueCount = uniqueValues.size();
        
        // Если только одно уникальное значение - это константа, бесполезна для группировки
        if (uniqueCount == 1) {
            log.debug("Cluster {} filtered: constant value", clusterKeys);
            return false;
        }

        // Если уникальность > 90% - вероятно это ID или серийный номер
        double cardinality = (double) uniqueCount / rowsWithAttribute;
        if (cardinality > MAX_CARDINALITY) {
            log.debug("Cluster {} filtered: cardinality {} > {} (likely ID/serial)", 
                    clusterKeys, cardinality, MAX_CARDINALITY);
            return false;
        }

        log.debug("Cluster {} passed significance filter (coverage: {}, cardinality: {})", 
                clusterKeys, coverage, cardinality);
        return true;
    }

    /**
     * Фильтрует список кластеров, оставляя только значимые
     * Обрабатывает кластеры как единый атрибут, объединяя значения из всех ключей кластера
     *
     * @param clusters список кластеров для фильтрации
     * @param rows список строк данных (каждая строка - мапа заголовок -> значение)
     * @return отфильтрованный список значимых кластеров
     */
    public List<List<String>> filterSignificantClusters(
            List<List<String>> clusters,
            List<Map<String, String>> rows) {

        if (rows == null || rows.isEmpty()) {
            log.warn("Empty rows data, cannot filter clusters");
            return List.of();
        }

        int totalRows = rows.size();
        log.info("Filtering {} clusters against {} rows", clusters.size(), totalRows);

        return clusters.stream()
                .filter(cluster -> isClusterSignificant(cluster, rows, totalRows))
                .toList();
    }

    /**
     * Проверяет значимость кластера, объединяя значения из всех ключей кластера
     * Концепция: кластер ["Weight", "Mass", "Вес"] обрабатывается как один атрибут
     * Для каждой строки берется первое не-null значение из ключей кластера
     *
     * @param clusterKeys список ключей в кластере
     * @param rows список строк данных
     * @param totalRows общее количество строк
     * @return true если кластер значим
     */
    private boolean isClusterSignificant(
            List<String> clusterKeys,
            List<Map<String, String>> rows,
            int totalRows) {

        if (clusterKeys == null || clusterKeys.isEmpty() || totalRows == 0) {
            return false;
        }

        // Собираем "эффективные" значения для кластера
        // Для каждой строки берем первое не-null значение из ключей кластера
        List<String> effectiveValues = rows.stream()
                .map(row -> {
                    // Ищем первое не-null значение среди ключей кластера
                    for (String key : clusterKeys) {
                        String value = row.get(key);
                        if (value != null && !value.trim().isEmpty() && !value.equalsIgnoreCase("NULL")) {
                            return value.trim();
                        }
                    }
                    return null;
                })
                .filter(value -> value != null)
                .toList();

        int rowsWithValue = effectiveValues.size();

        // 1. Coverage: проверяем покрытие
        double coverage = (double) rowsWithValue / totalRows;
        if (coverage < MIN_COVERAGE) {
            log.debug("Cluster {} filtered: coverage {} < {} (only {} rows with value)", 
                    clusterKeys, coverage, MIN_COVERAGE, rowsWithValue);
            return false;
        }

        // 2. Cardinality: проверяем уникальность
        long uniqueCount = effectiveValues.stream().distinct().count();

        // Если только одно уникальное значение - это константа
        if (uniqueCount == 1) {
            log.debug("Cluster {} filtered: constant value", clusterKeys);
            return false;
        }

        // Если уникальность > 90% - вероятно это ID
        double cardinality = (double) uniqueCount / rowsWithValue;
        if (cardinality > MAX_CARDINALITY) {
            log.debug("Cluster {} filtered: cardinality {} > {} (likely ID/serial, {} unique from {} rows)", 
                    clusterKeys, cardinality, MAX_CARDINALITY, uniqueCount, rowsWithValue);
            return false;
        }

        log.debug("Cluster {} passed significance filter (coverage: {}, cardinality: {}, unique: {}/{})", 
                clusterKeys, coverage, cardinality, uniqueCount, rowsWithValue);
        return true;
    }

    /**
     * Фильтрует список кластеров, оставляя только значимые (legacy метод для обратной совместимости)
     *
     * @param clusters список кластеров для фильтрации
     * @param sampleValues мапа: ключ -> значение из выборки данных
     * @param totalRows общее количество строк в данных
     * @return отфильтрованный список значимых кластеров
     */
    public List<List<String>> filterSignificantClusters(
            List<List<String>> clusters, 
            Map<String, String> sampleValues, 
            int totalRows) {
        
        return clusters.stream()
                .filter(cluster -> isSignificant(cluster, sampleValues, totalRows))
                .toList();
    }
}

