package ru.tenderhack.cte.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tenderhack.cte.dto.*;
import ru.tenderhack.cte.facade.GroupingFacade;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для операций группировки СТЕ
 */
@Slf4j
@RestController
@RequestMapping("/api/grouping")
@RequiredArgsConstructor
public class GroupingController {

    private final GroupingFacade groupingFacade;

    /**
     * 1) Отправка запроса на группировку СТЕ
     */
    @PostMapping("/request")
    public ResponseEntity<TaskResponse> createGroupingTask(
            @Valid @RequestBody GroupingRequest request
    ) {
        log.info("POST /api/grouping/request - query: {}", request.query());
        TaskResponse response = groupingFacade.createGroupingTask(request.query());
        return ResponseEntity.ok(response);
    }

    /**
     * 2) Получение результатов группировки (Карточек)
     */
    @GetMapping("/{taskId}/results")
    public ResponseEntity<PagedCteResponse> getGroupingResults(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Map<String, String> filters
    ) {
        log.info("GET /api/grouping/{}/results - page: {}, size: {}", taskId, page, size);
        
        // Убираем page и size из filters, если они туда попали
        if (filters != null) {
            filters.remove("page");
            filters.remove("size");
        }
        
        PagedCteResponse response = groupingFacade.getGroupingResults(taskId, page, size, filters);
        return ResponseEntity.ok(response);
    }

    /**
     * 3) Получение доступных фильтров
     */
    @GetMapping("/{taskId}/filters")
    public ResponseEntity<List<FilterOption>> getGroupingFilters(
            @PathVariable UUID taskId
    ) {
        log.info("GET /api/grouping/{}/filters", taskId);
        List<FilterOption> filters = groupingFacade.getGroupingFilters(taskId);
        return ResponseEntity.ok(filters);
    }

    /**
     * 4) Перегенерация группировки
     */
    @PostMapping("/{taskId}/regenerate")
    public ResponseEntity<TaskResponse> regenerateGrouping(
            @PathVariable UUID taskId,
            @Valid @RequestBody GroupingRequest request
    ) {
        log.info("POST /api/grouping/{}/regenerate - query: {}", taskId, request.query());
        TaskResponse response = groupingFacade.regenerateGrouping(taskId, request.query());
        return ResponseEntity.ok(response);
    }

    /**
     * 5) Подтверждение группировки (Approve)
     */
    @PostMapping("/{taskId}/approve")
    public ResponseEntity<Void> approveGrouping(
            @PathVariable UUID taskId
    ) {
        log.info("POST /api/grouping/{}/approve", taskId);
        groupingFacade.approveGrouping(taskId);
        return ResponseEntity.ok().build();
    }

    /**
     * 6) Оценка группировки
     */
    @PostMapping("/{taskId}/rate")
    public ResponseEntity<Void> rateGrouping(
            @PathVariable UUID taskId,
            @Valid @RequestBody RatingRequest request
    ) {
        log.info("POST /api/grouping/{}/rate - rating: {}", taskId, request.rating());
        groupingFacade.rateGrouping(taskId, request.rating());
        return ResponseEntity.ok().build();
    }
}

