package ru.brainnotfound.backend.security;

import java.time.LocalDateTime;

public record RefreshToken(String tokenValue, LocalDateTime issuedAt, LocalDateTime expiresAt) {
}
