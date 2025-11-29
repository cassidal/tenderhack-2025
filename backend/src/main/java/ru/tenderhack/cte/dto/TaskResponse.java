package ru.tenderhack.cte.dto;

import java.util.UUID;

/**
 * Ответ с идентификатором задачи группировки
 */
public record TaskResponse(
        UUID taskId
) {
}

