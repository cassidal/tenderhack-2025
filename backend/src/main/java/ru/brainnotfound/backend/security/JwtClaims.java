package ru.brainnotfound.backend.security;

import java.util.UUID;

public record JwtClaims(
        String login,
        UUID userId,
        UUID sessionId
) {
}
