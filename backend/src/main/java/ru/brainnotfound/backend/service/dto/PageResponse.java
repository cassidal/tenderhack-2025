package ru.brainnotfound.backend.service.dto;

import java.util.*;

public record PageResponse<T>(
        int pageNumber,
        int pageSize,

        long totalPages,
        long totalElements,

        List<T> data
) {
}
