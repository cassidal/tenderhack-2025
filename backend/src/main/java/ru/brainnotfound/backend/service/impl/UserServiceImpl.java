package ru.brainnotfound.backend.service.impl;

import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import ru.brainnotfound.backend.exception.*;
import ru.brainnotfound.backend.persistance.entity.*;
import ru.brainnotfound.backend.persistance.repository.*;
import ru.brainnotfound.backend.service.*;
import ru.brainnotfound.backend.service.dto.*;

import java.util.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUserFromDto(AuthRequest AuthRequest) {
        checkIfUsernameExistsThrowsValidationException(AuthRequest.username());

        User user = new User();
        user.setLogin(AuthRequest.username());
        user.setPassword(passwordEncoder.encode(AuthRequest.password()));

        return userRepository.save(user);
    }

    private void checkIfUsernameExistsThrowsValidationException(String username) throws ValidationException {
        Optional<User> user = userRepository.findByLoginIgnoreCase(username);

        if (user.isPresent()) {
            throw new ValidationException("Username is already taken");
        }
    }
}
