package ru.brainnotfound.backend.util;

import org.springframework.security.core.context.*;
import ru.brainnotfound.backend.security.principal.*;

public class SecurityUtils {
    private SecurityUtils() {
    }

    public static Principal getPrincipal() {
        return (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
