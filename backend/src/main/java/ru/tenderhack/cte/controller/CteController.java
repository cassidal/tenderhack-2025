package ru.tenderhack.cte.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tenderhack.cte.dto.CteDetail;
import ru.tenderhack.cte.facade.GroupingFacade;

import java.util.UUID;

/**
 * Контроллер для операций с СТЕ (Карточками)
 */
@Slf4j
@RestController
@RequestMapping("/api/cte")
@RequiredArgsConstructor
public class CteController {

    private final GroupingFacade groupingFacade;

    /**
     * 7) Получение детальной информации об СТЕ
     */
    @GetMapping("/{id}")
    public ResponseEntity<CteDetail> getCteDetails(
            @PathVariable UUID id
    ) {
        log.info("GET /api/cte/{}", id);
        CteDetail detail = groupingFacade.getCteDetails(id);
        return ResponseEntity.ok(detail);
    }
}

