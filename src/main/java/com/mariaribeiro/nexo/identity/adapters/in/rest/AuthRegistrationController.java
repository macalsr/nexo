package com.mariaribeiro.nexo.identity.adapters.in.rest;

import com.mariaribeiro.nexo.identity.application.auth.SignupCommand;
import com.mariaribeiro.nexo.identity.application.auth.SignupResult;
import com.mariaribeiro.nexo.identity.application.auth.SignupService;
import com.mariaribeiro.nexo.identity.application.verification.VerifyEmailCommand;
import com.mariaribeiro.nexo.identity.application.verification.VerifyEmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRegistrationController {

    private final SignupService signupService;
    private final VerifyEmailService verifyEmailService;
    private final RefreshCookieService refreshCookieService;

    @PostMapping("/signup")
    public ResponseEntity<AuthTokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResult result = signupService.signup(new SignupCommand(request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.buildRefreshCookie(result.refreshToken()))
                .body(new AuthTokenResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") @NotBlank(message = "Verification token is required") String token) {
        verifyEmailService.verify(new VerifyEmailCommand(token));
        return ResponseEntity.noContent().build();
    }
}
