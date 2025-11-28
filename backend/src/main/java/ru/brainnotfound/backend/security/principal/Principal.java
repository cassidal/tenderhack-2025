package ru.brainnotfound.backend.security.principal;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface Principal extends UserDetails {
    String SESSION_ID = "SESSION_ID";

    UUID getUserId();

    Object getProperty(String key);
}
