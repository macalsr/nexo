package com.mariaribeiro.nexo.identity.adapters.in.rest;

import com.mariaribeiro.nexo.identity.application.usecase.ForgotPasswordCommand;
import com.mariaribeiro.nexo.identity.application.usecase.ForgotPasswordUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.InvalidRefreshTokenException;
import com.mariaribeiro.nexo.identity.application.usecase.LoginCommand;
import com.mariaribeiro.nexo.identity.application.usecase.LoginResult;
import com.mariaribeiro.nexo.identity.application.usecase.LoginUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.LogoutAllUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.LogoutUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.RefreshSessionCommand;
import com.mariaribeiro.nexo.identity.application.usecase.RefreshSessionResult;
import com.mariaribeiro.nexo.identity.application.usecase.RefreshSessionUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.ResetPasswordCommand;
import com.mariaribeiro.nexo.identity.application.usecase.ResetPasswordUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.SignupCommand;
import com.mariaribeiro.nexo.identity.application.usecase.SignupResult;
import com.mariaribeiro.nexo.identity.application.usecase.SignupUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.VerifyEmailCommand;
import com.mariaribeiro.nexo.identity.application.usecase.VerifyEmailUseCase;
import com.mariaribeiro.nexo.identity.adapters.out.security.RefreshTokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
    private final RefreshTokenProperties refreshTokenProperties;

    public AuthController(
            LoginUseCase loginUseCase,
            SignupUseCase signupUseCase,
            ForgotPasswordUseCase forgotPasswordUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            VerifyEmailUseCase verifyEmailUseCase,
            RefreshSessionUseCase refreshSessionUseCase,
            LogoutUseCase logoutUseCase,
            LogoutAllUseCase logoutAllUseCase,
            RefreshTokenProperties refreshTokenProperties) {
        this.loginUseCase = loginUseCase;
        this.signupUseCase = signupUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.logoutUseCase = logoutUseCase;
        this.logoutAllUseCase = logoutAllUseCase;
        this.refreshTokenProperties = refreshTokenProperties;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken()).toString())
                .body(new LoginResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResult result = signupUseCase.signup(new SignupCommand(request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken()).toString())
                .body(new SignupResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request).orElseThrow(InvalidRefreshTokenException::new);
        RefreshSessionResult result = refreshSessionUseCase.refresh(new RefreshSessionCommand(refreshToken));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken()).toString())
                .body(new LoginResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        extractRefreshToken(request).ifPresent(token -> logoutUseCase.logout(new RefreshSessionCommand(token)));

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request).orElseThrow(InvalidRefreshTokenException::new);
        logoutAllUseCase.logoutAll(new RefreshSessionCommand(refreshToken));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
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

    private Optional<String> extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> refreshTokenProperties.getCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenProperties.getCookieName(), refreshToken)
                .httpOnly(true)
                .secure(refreshTokenProperties.isCookieSecure())
                .sameSite(refreshTokenProperties.getCookieSameSite())
                .path(refreshTokenProperties.getCookiePath())
                .maxAge(refreshTokenProperties.getTtl())
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(refreshTokenProperties.getCookieName(), "")
                .httpOnly(true)
                .secure(refreshTokenProperties.isCookieSecure())
                .sameSite(refreshTokenProperties.getCookieSameSite())
                .path(refreshTokenProperties.getCookiePath())
                .maxAge(Duration.ZERO)
                .build();
    }
}
