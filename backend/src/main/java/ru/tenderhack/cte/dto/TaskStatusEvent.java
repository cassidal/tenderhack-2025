package ru.tenderhack.cte.dto;

import ru.tenderhack.cte.entity.Status;

import java.time.Instant;
import java.util.UUID;

public record TaskStatusEvent(
        UUID taskId,
        Status status,
        Integer progress, // Например, 0-100%
        String message,
        Instant timestamp
) {
    // Удобный конструктор для быстрого создания
    public static TaskStatusEvent of(UUID taskId, Status status, String message) {
        return new TaskStatusEvent(taskId, status, null, message, Instant.now());
    }
}