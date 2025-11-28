package ru.brainnotfound.backend.service;

import ru.brainnotfound.backend.persistance.entity.*;
import ru.brainnotfound.backend.service.dto.*;

public interface UserService {
    User createUserFromDto(AuthRequest authDTO);
}
