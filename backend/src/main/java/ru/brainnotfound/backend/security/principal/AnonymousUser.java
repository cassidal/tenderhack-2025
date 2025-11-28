package ru.brainnotfound.backend.security.principal;

import org.springframework.security.core.*;

import java.util.*;

public record AnonymousUser() implements Principal {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public UUID getUserId() {
        return null;
    }

    @Override
    public Object getProperty(String key) {
        throw new NoSuchElementException("No such property: " + key);
    }
}
