package ru.tenderhack.cte.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tenderhack.cte.dto.*;
import ru.tenderhack.cte.entity.GroupingTaskEntity;
import ru.tenderhack.cte.entity.Status;
import ru.tenderhack.cte.exception.ResourceNotFoundException;
import ru.tenderhack.cte.repository.GroupingTaskRepository;
import ru.tenderhack.cte.service.GroupingService;
import ru.tenderhack.cte.service.LlmClientService;

import java.util.*;

/**
 * Stub-имплементация фасада группировки.
 * Возвращает захардкоженные данные для демонстрации.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupingFacadeStub implements GroupingFacade {

    private final GroupingTaskRepository taskRepository;
    private final LlmClientService llmClientService;
    private final GroupingService groupingService;

    // Хранилище stub-данных
    private static final Map<UUID, List<CteSummary>> TASK_CTE_MAP = new HashMap<>();
    private static final Map<UUID, CteDetail> CTE_DETAILS_MAP = new HashMap<>();

    @Override
    public TaskResponse createGroupingTask(String query) {
        log.info("Creating grouping task with query: {}", query);

        // Создаём реальную задачу в БД
        GroupingTaskEntity task = GroupingTaskEntity.builder()
                .query(query)
                .status(Status.COMPLETED) // Сразу ставим COMPLETED для stub
                .build();
        task = taskRepository.save(task);

        // Генерируем stub-данные СТЕ
        generateStubCtes(task.getId());
        groupingService.startGroupingTask(task.getId(), query);

        log.info("Created task with ID: {}", task.getId());
        return new TaskResponse(task.getId());
    }

    @Override
    public PagedCteResponse getGroupingResults(UUID taskId, int page, int size, Map<String, String> filters) {
        log.info("Getting grouping results for task: {}, page: {}, size: {}, filters: {}", 
                taskId, page, size, filters);

        validateTaskExists(taskId);

        List<CteSummary> allCtes = TASK_CTE_MAP.getOrDefault(taskId, generateStubCtes(taskId));
        
        // Применяем фильтрацию (простая stub-логика)
        List<CteSummary> filteredCtes = filterCtes(allCtes, filters);

        // Пагинация
        int totalElements = filteredCtes.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<CteSummary> pagedContent = filteredCtes.subList(fromIndex, toIndex);

        return new PagedCteResponse(pagedContent, totalPages, totalElements, size, page);
    }

    @Override
    public List<FilterOption> getGroupingFilters(UUID taskId) {
        log.info("Getting filters for task: {}", taskId);

        validateTaskExists(taskId);

        // Возвращаем stub-фильтры
        return List.of(
                new FilterOption("brand", "Бренд", List.of("Samsung", "LG", "Bosch", "Electrolux")),
                new FilterOption("material", "Материал", List.of("Нержавеющая сталь", "Пластик", "Керамика")),
                new FilterOption("color", "Цвет", List.of("Белый", "Серебристый", "Черный"))
        );
    }

    @Override
    public TaskResponse regenerateGrouping(UUID taskId, String query) {
        log.info("Regenerating grouping for task: {} with query: {}", taskId, query);

        GroupingTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        // Обновляем запрос
        task.setQuery(query);
        task.setStatus(Status.COMPLETED);
        taskRepository.save(task);

        // Перегенерируем stub-данные
        generateStubCtes(taskId);

        return new TaskResponse(taskId);
    }

    @Override
    public void approveGrouping(UUID taskId) {
        log.info("Approving grouping for task: {}", taskId);

        GroupingTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        task.setStatus(Status.APPROVED);
        taskRepository.save(task);
    }

    @Override
    public void rateGrouping(UUID taskId, int rating) {
        log.info("Rating grouping for task: {} with rating: {}", taskId, rating);

        GroupingTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        task.setRating(rating);
        taskRepository.save(task);
    }

    @Override
    public CteDetail getCteDetails(UUID cteId) {
        log.info("Getting CTE details for: {}", cteId);

        CteDetail detail = CTE_DETAILS_MAP.get(cteId);
        if (detail == null) {
            throw new ResourceNotFoundException("CTE not found: " + cteId);
        }
        return detail;
    }

    // ============ Вспомогательные методы ============

    private void validateTaskExists(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
    }

    private List<CteSummary> generateStubCtes(UUID taskId) {
        // Генерируем 3 stub СТЕ
        UUID cte1Id = UUID.randomUUID();
        UUID cte2Id = UUID.randomUUID();
        UUID cte3Id = UUID.randomUUID();

        List<CteSummary> summaries = List.of(
                new CteSummary(
                        cte1Id,
                        "https://example.com/images/samsung-washer.jpg",
                        List.of(
                                new Attribute("Бренд", "Samsung"),
                                new Attribute("Материал", "Нержавеющая сталь"),
                                new Attribute("Цвет", "Белый")
                        )
                ),
                new CteSummary(
                        cte2Id,
                        "https://example.com/images/lg-fridge.jpg",
                        List.of(
                                new Attribute("Бренд", "LG"),
                                new Attribute("Материал", "Пластик"),
                                new Attribute("Цвет", "Серебристый")
                        )
                ),
                new CteSummary(
                        cte3Id,
                        "https://example.com/images/bosch-dishwasher.jpg",
                        List.of(
                                new Attribute("Бренд", "Bosch"),
                                new Attribute("Материал", "Нержавеющая сталь"),
                                new Attribute("Цвет", "Черный")
                        )
                )
        );

        TASK_CTE_MAP.put(taskId, summaries);

        // Генерируем детальную информацию
        CTE_DETAILS_MAP.put(cte1Id, new CteDetail(
                cte1Id,
                "https://example.com/images/samsung-washer.jpg",
                List.of(
                        new Attribute("Бренд", "Samsung"),
                        new Attribute("Материал", "Нержавеющая сталь"),
                        new Attribute("Цвет", "Белый")
                ),
                List.of(
                        new Attribute("Гарантия", "2 года"),
                        new Attribute("Страна производства", "Южная Корея")
                ),
                List.of(1001L, 1002L, 1003L)
        ));

        CTE_DETAILS_MAP.put(cte2Id, new CteDetail(
                cte2Id,
                "https://example.com/images/lg-fridge.jpg",
                List.of(
                        new Attribute("Бренд", "LG"),
                        new Attribute("Материал", "Пластик"),
                        new Attribute("Цвет", "Серебристый")
                ),
                List.of(
                        new Attribute("Гарантия", "3 года"),
                        new Attribute("Энергоэффективность", "A++")
                ),
                List.of(2001L, 2002L)
        ));

        CTE_DETAILS_MAP.put(cte3Id, new CteDetail(
                cte3Id,
                "https://example.com/images/bosch-dishwasher.jpg",
                List.of(
                        new Attribute("Бренд", "Bosch"),
                        new Attribute("Материал", "Нержавеющая сталь"),
                        new Attribute("Цвет", "Черный")
                ),
                List.of(
                        new Attribute("Гарантия", "2 года"),
                        new Attribute("Уровень шума", "44 дБ")
                ),
                List.of(3001L, 3002L, 3003L, 3004L)
        ));

        return summaries;
    }

    private List<CteSummary> filterCtes(List<CteSummary> ctes, Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>(ctes);
        }

        return ctes.stream()
                .filter(cte -> matchesFilters(cte, filters))
                .toList();
    }

    private boolean matchesFilters(CteSummary cte, Map<String, String> filters) {
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String filterKey = filter.getKey();
            String filterValue = filter.getValue();

            boolean matches = cte.attributes().stream()
                    .anyMatch(attr -> 
                            attr.name().equalsIgnoreCase(filterKey) && 
                            attr.value().equalsIgnoreCase(filterValue));

            if (!matches) {
                return false;
            }
        }
        return true;
    }
}

