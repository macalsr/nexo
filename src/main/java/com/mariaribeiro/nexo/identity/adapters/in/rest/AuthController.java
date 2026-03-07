package com.mariaribeiro.nexo.identity.adapters.in.rest;

import com.mariaribeiro.nexo.identity.application.recovery.ForgotPasswordCommand;
import com.mariaribeiro.nexo.identity.application.recovery.ForgotPasswordUseCase;
import com.mariaribeiro.nexo.identity.application.auth.InvalidRefreshTokenException;
import com.mariaribeiro.nexo.identity.application.auth.LoginCommand;
import com.mariaribeiro.nexo.identity.application.auth.LoginResult;
import com.mariaribeiro.nexo.identity.application.auth.LoginUseCase;
import com.mariaribeiro.nexo.identity.application.auth.LogoutAllUseCase;
import com.mariaribeiro.nexo.identity.application.auth.LogoutUseCase;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionCommand;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionResult;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionUseCase;
import com.mariaribeiro.nexo.identity.application.recovery.ResetPasswordCommand;
import com.mariaribeiro.nexo.identity.application.recovery.ResetPasswordUseCase;
import com.mariaribeiro.nexo.identity.application.auth.SignupCommand;
import com.mariaribeiro.nexo.identity.application.auth.SignupResult;
import com.mariaribeiro.nexo.identity.application.auth.SignupUseCase;
import com.mariaribeiro.nexo.identity.application.verification.VerifyEmailCommand;
import com.mariaribeiro.nexo.identity.application.verification.VerifyEmailUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final SignupUseCase signupUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final LogoutUseCase logoutUseCase;
    private final LogoutAllUseCase logoutAllUseCase;
    private final RefreshCookieService refreshCookieService;

    public AuthController(
            LoginUseCase loginUseCase,
            SignupUseCase signupUseCase,
            ForgotPasswordUseCase forgotPasswordUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            VerifyEmailUseCase verifyEmailUseCase,
            RefreshSessionUseCase refreshSessionUseCase,
            LogoutUseCase logoutUseCase,
            LogoutAllUseCase logoutAllUseCase,
            RefreshCookieService refreshCookieService) {
        this.loginUseCase = loginUseCase;
        this.signupUseCase = signupUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.logoutUseCase = logoutUseCase;
        this.logoutAllUseCase = logoutAllUseCase;
        this.refreshCookieService = refreshCookieService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.buildRefreshCookie(result.refreshToken()))
                .body(new LoginResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResult result = signupUseCase.signup(new SignupCommand(request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.buildRefreshCookie(result.refreshToken()))
                .body(new SignupResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        String refreshToken = refreshCookieService.extractRefreshToken(request)
                .orElseThrow(InvalidRefreshTokenException::new);
        RefreshSessionResult result = refreshSessionUseCase.refresh(new RefreshSessionCommand(refreshToken));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.buildRefreshCookie(result.refreshToken()))
                .body(new LoginResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        refreshCookieService.extractRefreshToken(request)
                .ifPresent(token -> logoutUseCase.logout(new RefreshSessionCommand(token)));

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.clearRefreshCookie())
                .build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest request) {
        String refreshToken = refreshCookieService.extractRefreshToken(request)
                .orElseThrow(InvalidRefreshTokenException::new);
        logoutAllUseCase.logoutAll(new RefreshSessionCommand(refreshToken));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.clearRefreshCookie())
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.requestReset(new ForgotPasswordCommand(request.email()));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new AuthMessageResponse("Check your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(new ResetPasswordCommand(request.token(), request.newPassword()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") @NotBlank(message = "Verification token is required") String token) {
        verifyEmailUseCase.verify(new VerifyEmailCommand(token));
        return ResponseEntity.noContent().build();
    }
}

