package ru.brainnotfound.backend.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.brainnotfound.backend.security.principal.AuthUserDetails;
import ru.brainnotfound.backend.security.principal.Principal;
import ru.brainnotfound.backend.service.AuthService;
import ru.brainnotfound.backend.service.TokenGenerationService;
import ru.brainnotfound.backend.service.UserService;
import ru.brainnotfound.backend.service.dto.AuthRequest;
import ru.brainnotfound.backend.service.dto.TokenDto;

@Service
public class AuthServiceImpl implements AuthService {
    private final TokenGenerationService tokenGenerationService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(TokenGenerationService tokenGenerationService, UserService userService, AuthenticationManager authenticationManager) {
        this.tokenGenerationService = tokenGenerationService;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public TokenDto register(AuthRequest AuthRequest, String userAgent) {
        var user = userService.createUserFromDto(AuthRequest);
        var principal = AuthUserDetails.withUser(user);

        return tokenGenerationService.generateToken(principal, userAgent);
    }

    @Override
    public TokenDto login(AuthRequest AuthRequest, String userAgent) {
        var login = normalizeLogin(AuthRequest.username());
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, AuthRequest.password())
        );

        if (!(authentication.getPrincipal() instanceof Principal principal)) {
            throw new IllegalArgumentException("Principal is not of type Principal");
        }

        return tokenGenerationService.generateToken(principal, userAgent);
    }

    private String normalizeLogin(String login) {
        return login.toLowerCase().trim();
    }
}
