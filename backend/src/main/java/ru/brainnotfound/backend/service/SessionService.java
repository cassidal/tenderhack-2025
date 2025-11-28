package ru.brainnotfound.backend.service;

import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.*;
import ru.brainnotfound.backend.persistance.entity.*;
import ru.brainnotfound.backend.security.*;
import ru.brainnotfound.backend.security.principal.*;

import java.util.*;

public interface SessionService {
    @Transactional
    Session createSession(Principal principal, RefreshToken refreshToken, String deviceInfo);

    @Transactional(readOnly = true)
    Page<Session> getSessions(Principal principal, int pageNumber, int pageSize);

    @Transactional
    void revoke(Principal principal, UUID sessionId);

    @Transactional(readOnly = true)
    boolean isActive(UUID sessionId);

    @Transactional
    Session refreshSession(String oldRefreshToken, RefreshToken newRefreshToken);
}
