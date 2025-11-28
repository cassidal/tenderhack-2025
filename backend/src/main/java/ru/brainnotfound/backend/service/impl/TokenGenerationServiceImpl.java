package ru.brainnotfound.backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.brainnotfound.backend.persistance.entity.Session;
import ru.brainnotfound.backend.security.JwtClaims;
import ru.brainnotfound.backend.security.JwtUtils;
import ru.brainnotfound.backend.security.RefreshToken;
import ru.brainnotfound.backend.security.principal.Principal;
import ru.brainnotfound.backend.service.SessionService;
import ru.brainnotfound.backend.service.TokenGenerationService;
import ru.brainnotfound.backend.service.dto.TokenDto;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenGenerationServiceImpl implements TokenGenerationService {
    private static final Logger log = LoggerFactory.getLogger(TokenGenerationServiceImpl.class);

    private final SessionService sessionService;
    private final JwtUtils jwtUtils;
    private final int refreshTokenLiveDuration;

    public TokenGenerationServiceImpl(SessionService sessionService, JwtUtils jwtUtils, @Value("${refresh-token.durationDays}") int refreshTokenLiveDuration) {
        this.sessionService = sessionService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenLiveDuration = refreshTokenLiveDuration;
    }

    @Override
    @Transactional
    public TokenDto generateToken(Principal principal, String userAgent) {
        var session = sessionService.createSession(principal, generateRefreshToken(), userAgent);
        return tokenWithSession(session);
    }

    @Override
    @Transactional
    public TokenDto refreshToken(String refreshToken) {
        Session session = sessionService.refreshSession(refreshToken, generateRefreshToken());
        return tokenWithSession(session);
    }

    private RefreshToken generateRefreshToken() {
        var now = LocalDateTime.now(Clock.systemUTC());
        return new RefreshToken(UUID.randomUUID().toString(), now, now.plusDays(refreshTokenLiveDuration));
    }

    private TokenDto tokenWithSession(Session session) {
        var refreshToken = session.getRefreshToken();
        var jwtClaims = new JwtClaims(
                session.getUser().getLogin(),
                session.getUser().getId(),
                session.getId()
        );
        var accessToken = jwtUtils.generateToken(jwtClaims);
        log.debug("Access token: {}, refresh token: {}", accessToken, refreshToken);
        return new TokenDto(accessToken, refreshToken);
    }
}
