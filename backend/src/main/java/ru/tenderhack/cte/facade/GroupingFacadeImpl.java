package ru.tenderhack.cte.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tenderhack.cte.dto.*;
import ru.tenderhack.cte.entity.CteEntity;
import ru.tenderhack.cte.entity.GroupingTaskEntity;
import ru.tenderhack.cte.entity.Status;
import ru.tenderhack.cte.exception.ResourceNotFoundException;
import ru.tenderhack.cte.mapper.CteMapper;
import ru.tenderhack.cte.repository.CteRepository;
import ru.tenderhack.cte.repository.GroupingTaskRepository;
import ru.tenderhack.cte.service.GroupingPipelineService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Реализация фасада группировки с асинхронным pipeline
 */
@Slf4j
@Service
@org.springframework.context.annotation.Primary
@RequiredArgsConstructor
public class GroupingFacadeImpl implements GroupingFacade {

    private final GroupingTaskRepository taskRepository;
    private final CteRepository cteRepository;
    private final GroupingPipelineService pipelineService;
    private final CteMapper cteMapper;

    @Override
    @Transactional
    public TaskResponse createGroupingTask(String query) {
        log.info("Creating grouping task with query: {}", query);

        // Создаём задачу в БД со статусом IN_PROGRESS
        GroupingTaskEntity task = GroupingTaskEntity.builder()
                .query(query)
                .status(Status.IN_PROGRESS)
                .build();
        task = taskRepository.save(task);

        log.info("Task created with ID: {}, starting async pipeline...", task.getId());

        // Запускаем асинхронный pipeline
        CompletableFuture<Void> pipelineFuture = pipelineService.runPipeline(task.getId(), query);

        // Обрабатываем ошибки асинхронно
        GroupingTaskEntity finalTask = task;
        pipelineFuture.exceptionally(throwable -> {
            log.error("Pipeline failed for task: {}", finalTask.getId(), throwable);
            return null;
        });

        // Возвращаем taskId немедленно
        return new TaskResponse(task.getId());
    }

    @Override
    public PagedCteResponse getGroupingResults(UUID taskId, int page, int size, Map<String, String> filters) {
        log.info("Getting grouping results for task: {}, page: {}, size: {}, filters: {}", 
                taskId, page, size, filters);

        validateTaskExists(taskId);

        // Получаем CTE из базы данных
        Pageable pageable = PageRequest.of(page, size);
        Page<CteEntity> ctePage = cteRepository.findByTaskId(taskId, pageable);

        // Преобразуем в DTO
        List<CteSummary> summaries = ctePage.getContent().stream()
                .map(cteMapper::toSummary)
                .toList();

        // Применяем фильтрацию (если нужно)
        List<CteSummary> filteredSummaries = applyFilters(summaries, filters);

        return new PagedCteResponse(
                filteredSummaries,
                ctePage.getTotalPages(),
                (int) ctePage.getTotalElements(),
                size,
                page
        );
    }

    @Override
    public List<FilterOption> getGroupingFilters(UUID taskId) {
        log.info("Getting filters for task: {}", taskId);

        validateTaskExists(taskId);

        // Получаем все CTE для задачи
        List<CteEntity> ctes = cteRepository.findByTaskId(taskId);

        // Извлекаем уникальные атрибуты из important_attributes
        Map<String, Set<String>> attributeValues = new HashMap<>();

        for (CteEntity cte : ctes) {
            for (var attr : cte.getImportantAttributes()) {
                attributeValues
                        .computeIfAbsent(attr.getName(), k -> new HashSet<>())
                        .add(attr.getValue());
            }
        }

        // Преобразуем в FilterOption
        return attributeValues.entrySet().stream()
                .map(entry -> new FilterOption(
                        entry.getKey(),
                        entry.getKey(), // Можно добавить локализацию
                        new ArrayList<>(entry.getValue())
                ))
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse regenerateGrouping(UUID taskId, String query) {
        log.info("Regenerating grouping for task: {} with query: {}", taskId, query);

        GroupingTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        // Удаляем старые CTE
        cteRepository.deleteByTaskId(taskId);
        log.info("Deleted old CTE entities for task: {}", taskId);

        // Обновляем запрос и статус
        task.setQuery(query);
        task.setStatus(Status.IN_PROGRESS);
        taskRepository.save(task);

        // Запускаем pipeline заново
        CompletableFuture<Void> pipelineFuture = pipelineService.runPipeline(taskId, query);

        pipelineFuture.exceptionally(throwable -> {
            log.error("Pipeline failed for task: {}", taskId, throwable);
            return null;
        });

        return new TaskResponse(taskId);
    }

    @Override
    @Transactional
    public void approveGrouping(UUID taskId) {
        log.info("Approving grouping for task: {}", taskId);

        GroupingTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        task.setStatus(Status.APPROVED);
        taskRepository.save(task);
    }

    @Override
    @Transactional
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

        CteEntity cte = cteRepository.findById(cteId)
                .orElseThrow(() -> new ResourceNotFoundException("CTE not found: " + cteId));

        return cteMapper.toDetail(cte);
    }

    // ============ Вспомогательные методы ============

    private void validateTaskExists(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
    }

    private List<CteSummary> applyFilters(List<CteSummary> summaries, Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return summaries;
        }

        return summaries.stream()
                .filter(summary -> matchesFilters(summary, filters))
                .toList();
    }

    private boolean matchesFilters(CteSummary summary, Map<String, String> filters) {
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String filterKey = filter.getKey();
            String filterValue = filter.getValue();

            boolean matches = summary.attributes().stream()
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

