package ru.tenderhack.cte.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на создание/перегенерацию группировки
 */
public record GroupingRequest(
        @NotBlank(message = "Query cannot be blank")
        String query
) {
}

