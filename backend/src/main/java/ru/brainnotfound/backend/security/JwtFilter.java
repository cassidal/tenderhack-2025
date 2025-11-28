package ru.brainnotfound.backend.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.brainnotfound.backend.controller.ExceptionController;
import ru.brainnotfound.backend.exception.AuthenticationRequiredException;
import ru.brainnotfound.backend.security.principal.AnonymousUser;
import ru.brainnotfound.backend.security.principal.AuthenticatedUserDetails;
import ru.brainnotfound.backend.service.SessionService;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final ExceptionController exceptionController;
    private final SessionService sessionService;
    private final JwtUtils jwtUtils;

    public JwtFilter(ExceptionController exceptionController, SessionService sessionService, JwtUtils jwtUtils) {
        this.exceptionController = exceptionController;
        this.sessionService = sessionService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var bearerHeader = request.getHeader("Authorization");

        if (bearerHeader == null || !bearerHeader.startsWith("Bearer ")) {
            var anonymous = new AnonymousUser();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(anonymous, null, anonymous.getAuthorities())
            );
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var accessToken = bearerHeader.substring(7);
            var jwtClaims = jwtUtils.parse(accessToken);
            var principal = AuthenticatedUserDetails.withClaims(jwtClaims);

            if (!sessionService.isActive(principal.getSessionId())) {
                throw new AuthenticationRequiredException("Session not found");
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
            );
            filterChain.doFilter(request, response);
        } catch (JWTVerificationException ex) {
            exceptionController.badRequest(request, response, ex);
        } catch (AuthenticationException ex) {
            exceptionController.commence(request, response, ex);
        }
    }
}
