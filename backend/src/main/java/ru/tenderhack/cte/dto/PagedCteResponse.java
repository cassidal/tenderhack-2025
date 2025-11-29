package ru.tenderhack.cte.dto;

import java.util.List;

/**
 * Пагинированный ответ со списком СТЕ
 */
public record PagedCteResponse(
        List<CteSummary> content,
        int totalPages,
        long totalElements,
        int size,
        int number
) {
}

