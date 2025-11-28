package ru.brainnotfound.backend.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import ru.brainnotfound.backend.persistance.entity.User;
import ru.brainnotfound.backend.persistance.repository.*;
import ru.brainnotfound.backend.security.principal.*;

import java.util.*;

@Service
@Transactional
public class AuthDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public AuthDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> foundUser = userRepository.findByLoginIgnoreCase(username);

        if (foundUser.isEmpty())
            throw new UsernameNotFoundException("User with username not found!");

        return AuthUserDetails.withUser(foundUser.get());
    }
}
