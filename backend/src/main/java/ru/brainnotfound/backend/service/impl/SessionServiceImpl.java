package ru.brainnotfound.backend.service.impl;

import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import ru.brainnotfound.backend.exception.*;
import ru.brainnotfound.backend.persistance.entity.*;
import ru.brainnotfound.backend.persistance.repository.*;
import ru.brainnotfound.backend.security.*;
import ru.brainnotfound.backend.security.principal.*;
import ru.brainnotfound.backend.service.*;

import java.time.*;
import java.util.*;

@Slf4j
@Component
@Transactional
public class SessionServiceImpl implements SessionService {
    private final SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Session createSession(Principal principal, RefreshToken refreshToken, String deviceInfo) {
        var user = new User();
        user.setId(principal.getUserId());
        user.setLogin(principal.getUsername());

        var session = new Session();
        session.setDeviceInfo(deviceInfo);
        session.setRefreshToken(refreshToken.tokenValue());
        session.setCreatedAt(refreshToken.issuedAt());
        session.setExpiresAt(refreshToken.expiresAt());
        session.setUser(user);

        session = sessionRepository.save(session);

        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Session> getSessions(Principal principal, int pageNumber, int pageSize) {
        var userId = principal.getUserId();
        return sessionRepository.findAllByUserId(userId, PageRequest.of(pageNumber, pageSize));
    }

    @Override
    public void revoke(Principal principal, UUID sessionId) {
        var session = sessionRepository.findById(sessionId);
        if (session.isEmpty()) {
            throw new SessionException("Session not found");
        }

        var userId = principal.getUserId();
        if (!session.get().getUser().getId().equals(userId)) {
            throw new SessionException("Session does not belong the user");
        }

        sessionRepository.delete(session.get());
    }

    @Override
    public boolean isActive(UUID sessionId) {
        Optional<Session> session = sessionRepository.findById(sessionId);

        return session.isPresent() && session.get().getExpiresAt().isAfter(LocalDateTime.now(Clock.systemUTC()));
    }

    @Override
    public Session refreshSession(String oldRefreshToken, RefreshToken newRefreshToken) {
        Optional<Session> oldSession = sessionRepository.findOneByRefreshToken(oldRefreshToken);
        if (oldSession.isEmpty()) {
            throw new SessionException("Session not found");
        } else if (oldSession.get().getExpiresAt().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
            throw new SessionException("Session expired");
        }

        Session newSession = new Session();
        newSession.setDeviceInfo(oldSession.get().getDeviceInfo());
        newSession.setRefreshToken(newRefreshToken.tokenValue());
        newSession.setCreatedAt(newRefreshToken.issuedAt());
        newSession.setExpiresAt(newRefreshToken.expiresAt());
        newSession.setUser(oldSession.get().getUser());

        newSession = sessionRepository.save(newSession);
        sessionRepository.delete(oldSession.get());

        return newSession;
    }
}
