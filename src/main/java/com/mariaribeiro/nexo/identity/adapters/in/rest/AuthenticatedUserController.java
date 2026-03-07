package com.mariaribeiro.nexo.identity.adapters.in.rest;

import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.usecase.AuthenticatedUserView;
import com.mariaribeiro.nexo.identity.application.usecase.ResendVerificationEmailUseCase;
import com.mariaribeiro.nexo.identity.adapters.out.security.AuthenticatedUserContext;
import com.mariaribeiro.nexo.identity.adapters.out.security.AuthenticationRequestContext;
import com.mariaribeiro.nexo.identity.adapters.out.security.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticatedUserController {

    private final AuthenticationRequestContext authenticationRequestContext;
    private final LoadUserByEmailPort loadUserByEmailPort;
    private final ResendVerificationEmailUseCase resendVerificationEmailUseCase;

    public AuthenticatedUserController(
            AuthenticationRequestContext authenticationRequestContext,
            LoadUserByEmailPort loadUserByEmailPort,
            ResendVerificationEmailUseCase resendVerificationEmailUseCase) {
        this.authenticationRequestContext = authenticationRequestContext;
        this.loadUserByEmailPort = loadUserByEmailPort;
        this.resendVerificationEmailUseCase = resendVerificationEmailUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(HttpServletRequest request) {
        AuthenticatedUserContext authenticatedUser = authenticationRequestContext.getAuthenticatedUser(request)
                .orElseThrow(UnauthorizedException::new);
        AuthenticatedUserView user = loadUserByEmailPort.findByEmail(authenticatedUser.email())
                .orElseThrow(UnauthorizedException::new);

        return ResponseEntity.ok(new AuthenticatedUserResponse(
                authenticatedUser.userId(),
                authenticatedUser.email(),
                user.emailVerified()));
    }

    @PostMapping("/auth/resend-verification")
    public ResponseEntity<AuthMessageResponse> resendVerification(HttpServletRequest request) {
        AuthenticatedUserContext authenticatedUser = authenticationRequestContext.getAuthenticatedUser(request)
                .orElseThrow(UnauthorizedException::new);

        resendVerificationEmailUseCase.resend(authenticatedUser.email());
        return ResponseEntity.accepted()
                .body(new AuthMessageResponse("Check your email"));
    }
}
