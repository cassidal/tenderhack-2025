package ru.brainnotfound.backend.util.validation;

import org.jetbrains.annotations.*;
import ru.brainnotfound.backend.exception.*;

public interface Validator<T> {
    boolean isSupported(@NotNull Class<?> clz);

    void validate(@NotNull T object) throws ValidationException;
}
