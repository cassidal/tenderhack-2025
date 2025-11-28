package ru.brainnotfound.backend.service.dto;

public record AuthRequest(
        String username,
        String password
) {
}
