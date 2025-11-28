package ru.brainnotfound.backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {
    public static final String USER_ID_CLAIM = "user_id";
    public static final String SESSION_ID_CLAIM = "sid";

    private final JwtProperties jwtProperties;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }


    public String generateToken(JwtClaims claims) {
        var now = Instant.now();
        var expirationDate = Date.from(now.plus(jwtProperties.expirationDays, ChronoUnit.DAYS));

        return JWT.create()
                .withSubject(claims.login())
                .withIssuer(jwtProperties.issuer)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(expirationDate)
                .withClaim(USER_ID_CLAIM, claims.userId().toString())
                .withClaim(SESSION_ID_CLAIM, claims.sessionId().toString())
                .sign(Algorithm.HMAC256(jwtProperties.secretWord));
    }

    public JwtClaims parse(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtProperties.secretWord))
                .withIssuer(jwtProperties.issuer)
                .withClaimPresence(USER_ID_CLAIM)
                .withClaimPresence(SESSION_ID_CLAIM)
                .build();

        DecodedJWT jwt = verifier.verify(token);

        return new JwtClaims(
                jwt.getSubject(),
                jwt.getClaim(USER_ID_CLAIM).as(UUID.class),
                jwt.getClaim(SESSION_ID_CLAIM).as(UUID.class)
        );
    }
}
