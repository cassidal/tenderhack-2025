package ru.tenderhack.cte.entity;

/**
 * Статус задачи группировки
 */
public enum Status {
    PENDING,
    IN_PROGRESS,
    PROCESSING,  // Alias for IN_PROGRESS (backward compatibility)
    COMPLETED,
    APPROVED,
    FAILED
}

