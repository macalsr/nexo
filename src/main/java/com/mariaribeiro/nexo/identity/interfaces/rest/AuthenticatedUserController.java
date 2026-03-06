package com.mariaribeiro.nexo.identity.interfaces.rest;

import com.mariaribeiro.nexo.identity.infrastructure.security.AuthenticatedUserContext;
import com.mariaribeiro.nexo.identity.infrastructure.security.AuthenticationRequestContext;
import com.mariaribeiro.nexo.identity.infrastructure.security.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticatedUserController {

    private final AuthenticationRequestContext authenticationRequestContext;

    public AuthenticatedUserController(AuthenticationRequestContext authenticationRequestContext) {
        this.authenticationRequestContext = authenticationRequestContext;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(HttpServletRequest request) {
        AuthenticatedUserContext authenticatedUser = authenticationRequestContext.getAuthenticatedUser(request)
                .orElseThrow(UnauthorizedException::new);

        return ResponseEntity.ok(new AuthenticatedUserResponse(
                authenticatedUser.userId(),
                authenticatedUser.email()));
    }
}
