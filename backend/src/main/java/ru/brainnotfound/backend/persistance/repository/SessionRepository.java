package ru.brainnotfound.backend.persistance.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;
import ru.brainnotfound.backend.persistance.entity.*;

import java.util.*;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Page<Session> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Session> findOneByRefreshToken(String refreshToken);
}
