package ru.tenderhack.cte.dto;

import java.util.List;
import java.util.UUID;

/**
 * Краткая информация о СТЕ для отображения в списке
 */
public record CteSummary(
        UUID id,
        String imageUrl,
        List<Attribute> attributes
) {
}

