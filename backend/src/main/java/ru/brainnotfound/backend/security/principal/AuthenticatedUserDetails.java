package ru.brainnotfound.backend.security.principal;

import lombok.*;
import org.springframework.security.core.*;
import ru.brainnotfound.backend.security.*;

import java.util.*;

@Getter
public class AuthenticatedUserDetails implements Principal {
    private final UUID userId;
    private final UUID sessionId;
    private final String login;
    private final Map<String, Object> properties;

    private AuthenticatedUserDetails(String login, UUID userId, UUID sessionId, Map<String, Object> properties) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.login = login;
        this.properties = properties;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public UUID getUserId() {
        return userId;
    }

    @Override
    public Object getProperty(String key) {
        throw new NoSuchElementException("No such property: " + key);
    }

    public static AuthenticatedUserDetails withClaims(JwtClaims claims) {
        var properties = Map.<String, Object>of(
                Principal.SESSION_ID, claims.sessionId()
        );
        return new AuthenticatedUserDetails(
                claims.login(),
                claims.userId(),
                claims.sessionId(),
                properties
        );
    }
}
