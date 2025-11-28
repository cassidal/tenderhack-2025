package ru.brainnotfound.backend.util.validation;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractValidator<T> implements Validator<T> {
    private final Class<T> type;

    protected AbstractValidator(Class<T> type) {
        this.type = type;
    }

    public boolean isSupported(@NotNull Class<?> clz) {
        return type.isAssignableFrom(clz);
    }
}
