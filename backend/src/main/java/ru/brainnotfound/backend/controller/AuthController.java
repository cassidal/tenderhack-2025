package ru.brainnotfound.backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.brainnotfound.backend.service.AuthService;
import ru.brainnotfound.backend.service.RefreshTokenCookieService;
import ru.brainnotfound.backend.service.TokenGenerationService;
import ru.brainnotfound.backend.service.dto.AuthRequest;
import ru.brainnotfound.backend.service.dto.TokenDto;
import ru.brainnotfound.backend.service.dto.TokenResponse;
import ru.brainnotfound.backend.util.Constants;
import ru.brainnotfound.backend.util.validation.Validator;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final TokenGenerationService tokenGenerationService;
    private final Validator<AuthRequest> authValidator;
    private final RefreshTokenCookieService refreshTokenCookieService;

    public AuthController(AuthService authService, TokenGenerationService tokenGenerationService, Validator<AuthRequest> authValidator, RefreshTokenCookieService refreshTokenCookieService) {
        this.authService = authService;
        this.tokenGenerationService = tokenGenerationService;
        this.authValidator = authValidator;
        this.refreshTokenCookieService = refreshTokenCookieService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody AuthRequest request,
            @RequestHeader(value = "User-Agent", required = false) String device) {
        authValidator.validate(request);

        return formAnswer(authService.login(request, device));
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(
            @RequestBody AuthRequest request,
            @RequestHeader(value = "User-Agent", required = false) String device) {
        authValidator.validate(request);

        return formAnswer(authService.register(request, device));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@CookieValue(name = Constants.REFRESH_TOKEN_COOKIE_NAME) String refreshTokenFromCookie) {
        var refreshToken = refreshTokenCookieService.decryptRefreshToken(refreshTokenFromCookie);

        return formAnswer(tokenGenerationService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<TokenResponse> logout() {
        var headers = new HttpHeaders();
        refreshTokenCookieService.generateExpiredCookie(headers);

        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    private ResponseEntity<TokenResponse> formAnswer(TokenDto token) {
        var body = new TokenResponse(token.accessToken());
        var headers = new HttpHeaders();
        refreshTokenCookieService.putRefreshTokenToCookie(headers, token.refreshToken());

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}