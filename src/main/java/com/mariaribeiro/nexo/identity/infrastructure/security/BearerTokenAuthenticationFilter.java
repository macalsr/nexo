package com.mariaribeiro.nexo.identity.infrastructure.security;

import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import com.mariaribeiro.nexo.identity.application.usecase.TokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenServicePort tokenServicePort;
    private final AuthenticationRequestContext authenticationRequestContext;

    public BearerTokenAuthenticationFilter(
            TokenServicePort tokenServicePort,
            AuthenticationRequestContext authenticationRequestContext) {
        this.tokenServicePort = tokenServicePort;
        this.authenticationRequestContext = authenticationRequestContext;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return HttpMethod.OPTIONS.matches(request.getMethod())
                || path.equals("/health")
                || path.equals("/auth/login")
                || path.equals("/auth/signup")
                || path.equals("/auth/forgot-password")
                || path.equals("/auth/reset-password")
                || path.equals("/auth/verify-email")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            writeUnauthorizedResponse(response);
            return;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            writeUnauthorizedResponse(response);
            return;
        }

        try {
            TokenClaims tokenClaims = tokenServicePort.verify(token);
            authenticationRequestContext.setAuthenticatedUser(
                    request,
                    new AuthenticatedUserContext(tokenClaims.userId(), tokenClaims.email()));
            filterChain.doFilter(request, response);
        } catch (InvalidTokenException exception) {
            writeUnauthorizedResponse(response);
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"Unauthorized\"}");
    }
}
