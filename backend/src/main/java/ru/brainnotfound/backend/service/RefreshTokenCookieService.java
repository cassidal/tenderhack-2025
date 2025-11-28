package ru.brainnotfound.backend.service;

import org.springframework.http.HttpHeaders;

public interface RefreshTokenCookieService {
    void putRefreshTokenToCookie(HttpHeaders headers, String s);

    String decryptRefreshToken(String refreshTokenFromCookie);

    void generateExpiredCookie(HttpHeaders headers);
}
