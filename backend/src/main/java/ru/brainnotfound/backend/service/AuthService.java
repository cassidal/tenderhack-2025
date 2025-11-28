package ru.brainnotfound.backend.service;

import org.springframework.transaction.annotation.Transactional;
import ru.brainnotfound.backend.service.dto.AuthRequest;
import ru.brainnotfound.backend.service.dto.TokenDto;

public interface AuthService {
    @Transactional
    TokenDto register(AuthRequest authDTO, String userAgent);

    @Transactional
    TokenDto login(AuthRequest authDto, String userAgent);
}
