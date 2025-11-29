package ru.tenderhack.cte.dto;

import java.util.List;

/**
 * Опция фильтра для UI
 */
public record FilterOption(
        String key,
        String label,
        List<String> possibleValues
) {
}

