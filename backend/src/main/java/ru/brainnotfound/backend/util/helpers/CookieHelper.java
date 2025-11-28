package ru.brainnotfound.backend.util.helpers;

import jakarta.servlet.http.Cookie;
import lombok.NonNull;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

import static java.util.Objects.isNull;

@Component
public class CookieHelper {

    private final String cookieDomain;
    private final Boolean httpOnly;
    private final Boolean secure;

    public CookieHelper(@Value("${cookieHelper.domain}") String cookieDomain, @Value("${cookieHelper.httpOnly}") Boolean httpOnly, @Value("${cookieHelper.secure}") Boolean secure) {
        this.cookieDomain = cookieDomain;
        this.httpOnly = httpOnly;
        this.secure = secure;
    }

    public Optional<String> retrieve(Cookie[] cookies, @NonNull String name) {
        if (isNull(cookies)) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(name)) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    public String generateCookie(@NonNull String name, @NonNull String value, @NonNull Duration maxAge) {
        // Build cookie instance
        Cookie cookie = new Cookie(name, value);
        if (!"localhost".equals(cookieDomain)) { // https://stackoverflow.com/a/1188145
            cookie.setDomain(cookieDomain);
        }
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setMaxAge((int) maxAge.toSeconds());
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Strict");
        // Generate cookie string
        Rfc6265CookieProcessor processor = new Rfc6265CookieProcessor();
        return processor.generateHeader(cookie, null);
    }

    public String generateExpiredCookie(@NonNull String name) {
        return generateCookie(name, "-", Duration.ZERO);
    }

}
