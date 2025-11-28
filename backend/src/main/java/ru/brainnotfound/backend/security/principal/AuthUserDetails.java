package ru.brainnotfound.backend.security.principal;

import org.springframework.security.core.*;
import ru.brainnotfound.backend.persistance.entity.*;

import java.util.*;

public class AuthUserDetails implements Principal {
    private final User user;

    private AuthUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getLogin();
    }

    public static AuthUserDetails withUser(User user) {
        return new AuthUserDetails(user);
    }

    @Override
    public UUID getUserId() {
        return user.getId();
    }

    @Override
    public Object getProperty(String key) {
        throw new NoSuchElementException("No such property: " + key);
    }
}
