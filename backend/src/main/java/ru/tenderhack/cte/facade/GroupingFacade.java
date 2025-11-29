package ru.tenderhack.cte.facade;

import ru.tenderhack.cte.dto.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Фасад для оркестрации операций группировки СТЕ
 */
public interface GroupingFacade {

    /**
     * Создает новую задачу группировки
     */
    TaskResponse createGroupingTask(String query);

    /**
     * Получает результаты группировки с пагинацией и фильтрацией
     */
    PagedCteResponse getGroupingResults(UUID taskId, int page, int size, Map<String, String> filters);

    /**
     * Получает доступные фильтры для задачи
     */
    List<FilterOption> getGroupingFilters(UUID taskId);

    /**
     * Перегенерирует группировку с новым запросом
     */
    TaskResponse regenerateGrouping(UUID taskId, String query);

    /**
     * Подтверждает группировку
     */
    void approveGrouping(UUID taskId);

    /**
     * Оценивает группировку
     */
    void rateGrouping(UUID taskId, int rating);

    /**
     * Получает детальную информацию о СТЕ
     */
    CteDetail getCteDetails(UUID cteId);
}

