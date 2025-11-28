package ru.brainnotfound.backend.controller;

import com.github.f4b6a3.uuid.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.brainnotfound.backend.exception.*;
import ru.brainnotfound.backend.service.*;
import ru.brainnotfound.backend.service.dto.*;
import ru.brainnotfound.backend.util.*;
import ru.brainnotfound.backend.util.validation.*;

import java.util.*;

@RestController
@RequestMapping("/sessions")
public class SessionController {
    private final SessionService sessionService;
    private final Validator<PageRequest> pageRequestValidator;

    public SessionController(SessionService sessionService, Validator<PageRequest> pageRequestValidator) {
        this.sessionService = sessionService;
        this.pageRequestValidator = pageRequestValidator;
    }

    @PostMapping
    public ResponseEntity<PageResponse<SessionResponse>> getSessions(@RequestBody PageRequest pageRequest) {
        pageRequestValidator.validate(pageRequest);
        var page = sessionService.getSessions(SecurityUtils.getPrincipal(), pageRequest.pageNumber(), pageRequest.pageSize());

        return ResponseEntity.ok(ConvertUtils.convertToPageResponse(page, ConvertUtils::convertToSessionResponse));
    }

    @PostMapping("/{sessionId}/revoke")
    public ResponseEntity<?> revokeSession(@PathVariable String sessionId) {
        if (!UuidValidator.isValid(sessionId)) {
            throw new ValidationException("Invalid session id");
        }

        sessionService.revoke(SecurityUtils.getPrincipal(), UUID.fromString(sessionId));

        return ResponseEntity.noContent().build();
    }
}
