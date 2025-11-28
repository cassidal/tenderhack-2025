package ru.brainnotfound.backend.service;

import org.springframework.transaction.annotation.Transactional;
import ru.brainnotfound.backend.security.principal.Principal;
import ru.brainnotfound.backend.service.dto.TokenDto;

public interface TokenGenerationService {
    @Transactional
    TokenDto generateToken(Principal principal, String userAgent);

    @Transactional
    TokenDto refreshToken(String refreshToken);
}
