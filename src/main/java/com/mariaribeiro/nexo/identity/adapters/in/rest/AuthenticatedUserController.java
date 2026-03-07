package com.mariaribeiro.nexo.identity.adapters.in.rest;

import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.auth.AuthenticatedUserView;
import com.mariaribeiro.nexo.identity.application.verification.ResendVerificationEmailService;
import com.mariaribeiro.nexo.identity.adapters.in.security.AuthenticatedUserContext;
import com.mariaribeiro.nexo.identity.adapters.in.security.AuthenticationRequestContext;
import com.mariaribeiro.nexo.identity.adapters.out.security.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticatedUserController {

    private final AuthenticationRequestContext authenticationRequestContext;
    private final LoadUserByEmailPort loadUserByEmailPort;
    private final ResendVerificationEmailService resendVerificationEmailService;

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

        resendVerificationEmailService.resend(authenticatedUser.email());
        return ResponseEntity.accepted()
                .body(new AuthMessageResponse("Check your email"));
    }
}

