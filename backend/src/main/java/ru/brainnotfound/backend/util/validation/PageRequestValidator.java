package ru.brainnotfound.backend.util.validation;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.brainnotfound.backend.exception.ValidationException;
import ru.brainnotfound.backend.service.dto.PageRequest;

@Component
public class PageRequestValidator extends AbstractValidator<PageRequest> {
    public PageRequestValidator() {
        super(PageRequest.class);
    }

    @Override
    public void validate(@NotNull PageRequest object) throws ValidationException {
        var pageNumber = object.pageNumber();
        var pageSize = object.pageSize();

        if (pageNumber < 0) {
            throw new ValidationException("Page number cannot be negative");
        }
        if (pageSize <= 0) {
            throw new ValidationException("Page size should be greater than 0");
        }
    }
}
