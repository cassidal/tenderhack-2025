package ru.brainnotfound.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {
    public final String secretWord;
    public final String issuer;
    public final int expirationDays;

    public JwtProperties(@Value("${jwt.secret}") String secretWord,
                         @Value("${jwt.issuer}") String issuer,
                         @Value("${jwt.durationDays}") int expirationDays) {
        this.secretWord = secretWord;
        this.issuer = issuer;
        this.expirationDays = expirationDays;
    }
}
