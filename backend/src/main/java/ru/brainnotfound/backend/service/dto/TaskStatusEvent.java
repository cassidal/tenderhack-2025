package ru.brainnotfound.backend.service.dto;

import java.time.Instant;
import java.util.UUID;

public record TaskStatusEvent(
        UUID taskId,
        TaskStatus status,
        Integer progress, // Например, 0-100%
        String message,
        Instant timestamp
) {
    // Удобный конструктор для быстрого создания
    public static TaskStatusEvent of(UUID taskId, TaskStatus status, String message) {
        return new TaskStatusEvent(taskId, status, null, message, Instant.now());
    }
}