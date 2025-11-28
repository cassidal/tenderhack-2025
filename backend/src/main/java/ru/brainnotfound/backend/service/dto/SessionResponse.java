package ru.brainnotfound.backend.service.dto;

import java.time.*;

public record SessionResponse(String id, String deviceInfo, LocalDateTime createdAt, LocalDateTime expiresAt) {
}
