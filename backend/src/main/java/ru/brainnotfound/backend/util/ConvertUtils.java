package ru.brainnotfound.backend.util;

import org.springframework.core.convert.converter.*;
import org.springframework.data.domain.*;
import ru.brainnotfound.backend.persistance.entity.*;
import ru.brainnotfound.backend.service.dto.*;

public class ConvertUtils {
    private ConvertUtils() {
    }

    public static <T, S> PageResponse<S> convertToPageResponse(Page<T> page, Converter<T, S> converter) {
        var content = page.getContent();

        return new PageResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                content.stream().map(converter::convert).toList()
        );
    }

    public static SessionResponse convertToSessionResponse(Session session) {
        return new SessionResponse(
                session.getId().toString(),
                session.getDeviceInfo(),
                session.getCreatedAt(),
                session.getExpiresAt()
        );
    }
}
