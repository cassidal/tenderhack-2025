package ru.brainnotfound.backend.persistance.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;
import ru.brainnotfound.backend.persistance.entity.*;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLoginIgnoreCase(String login);
}
