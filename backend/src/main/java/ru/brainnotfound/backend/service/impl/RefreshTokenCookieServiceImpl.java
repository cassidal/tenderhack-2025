package ru.brainnotfound.backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import ru.brainnotfound.backend.service.RefreshTokenCookieService;
import ru.brainnotfound.backend.util.helpers.CookieHelper;
import ru.brainnotfound.backend.util.helpers.EncryptionHelper;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Base64;

import static ru.brainnotfound.backend.util.Constants.REFRESH_TOKEN_COOKIE_NAME;

@Service
public class RefreshTokenCookieServiceImpl implements RefreshTokenCookieService {
    private static final Base64.Encoder B64E = Base64.getEncoder();
    private static final Base64.Decoder B64D = Base64.getDecoder();

    private final SecretKey cookieSecretKey;
    private final CookieHelper cookieHelper;
    private final long refreshTokenDurationDays;

    public RefreshTokenCookieServiceImpl(SecretKey cookieSecretKey, CookieHelper cookieHelper, @Value("${refresh-token.durationDays}") int refreshTokenDurationDays) {
        this.cookieSecretKey = cookieSecretKey;
        this.cookieHelper = cookieHelper;
        this.refreshTokenDurationDays = refreshTokenDurationDays;
    }

    @Override
    public void putRefreshTokenToCookie(HttpHeaders headers, String refreshToken) {
        var encrypted = B64E.encodeToString(EncryptionHelper.encrypt(cookieSecretKey, refreshToken.getBytes()));
        var cookie = cookieHelper.generateCookie(REFRESH_TOKEN_COOKIE_NAME, encrypted, Duration.ofDays(refreshTokenDurationDays));
        headers.add(HttpHeaders.SET_COOKIE, cookie);
    }

    @Override
    public String decryptRefreshToken(String refreshTokenFromCookie) {
        var encrypted = B64D.decode(refreshTokenFromCookie);
        var bytes = EncryptionHelper.decrypt(cookieSecretKey, encrypted);
        return new String(bytes);
    }

    @Override
    public void generateExpiredCookie(HttpHeaders headers) {
        var cookie = cookieHelper.generateExpiredCookie(REFRESH_TOKEN_COOKIE_NAME);
        headers.add(HttpHeaders.SET_COOKIE, cookie);
    }
}
