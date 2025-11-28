package ru.brainnotfound.backend.service.dto;

public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
