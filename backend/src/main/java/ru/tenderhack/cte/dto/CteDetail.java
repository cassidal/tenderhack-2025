package ru.tenderhack.cte.dto;

import java.util.List;
import java.util.UUID;

/**
 * Детальная информация о СТЕ
 */
public record CteDetail(
        UUID id,
        String imageUrl,
        List<Attribute> importantAttributes,
        List<Attribute> secondaryAttributes,
        List<Long> productIds
) {
}

