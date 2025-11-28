package ru.brainnotfound.backend.util.validation;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.brainnotfound.backend.exception.ValidationException;
import ru.brainnotfound.backend.service.dto.AuthRequest;

@Component
public class AuthRequestValidator extends AbstractValidator<AuthRequest> {

    public AuthRequestValidator() {
        super(AuthRequest.class);
    }

    @Override
    public void validate(@NotNull AuthRequest dto) throws ValidationException {
        if (dto.password() == null || dto.username() == null) {
            throw new ValidationException("username and password are required");
        }

        var login = dto.username().trim();
        var password = dto.password();

        if (password.length() < 8) {
            throw new ValidationException("Password should be at least 8 characters");
        }
        if (login.length() < 3) {
            throw new ValidationException("Login length should be at least 3");
        }
    }
}
