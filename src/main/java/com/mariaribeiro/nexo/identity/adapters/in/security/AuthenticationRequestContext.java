package com.mariaribeiro.nexo.identity.adapters.in.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationRequestContext {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    public Optional<AuthenticatedUserContext> getAuthenticatedUser(HttpServletRequest request) {
        Object attribute = request.getAttribute(AUTHENTICATED_USER_ATTRIBUTE);
        if (attribute instanceof AuthenticatedUserContext authenticatedUserContext) {
            return Optional.of(authenticatedUserContext);
        }

        return Optional.empty();
    }

    public void setAuthenticatedUser(HttpServletRequest request, AuthenticatedUserContext authenticatedUserContext) {
        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUserContext);
    }
}

