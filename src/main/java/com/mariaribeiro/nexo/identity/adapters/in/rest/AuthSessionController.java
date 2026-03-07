package com.mariaribeiro.nexo.identity.adapters.in.rest;

import com.mariaribeiro.nexo.identity.application.auth.InvalidRefreshTokenException;
import com.mariaribeiro.nexo.identity.application.auth.LoginCommand;
import com.mariaribeiro.nexo.identity.application.auth.LoginResult;
import com.mariaribeiro.nexo.identity.application.auth.LoginService;
import com.mariaribeiro.nexo.identity.application.auth.LogoutAllService;
import com.mariaribeiro.nexo.identity.application.auth.LogoutService;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionCommand;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionResult;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthSessionController {

    private final LoginService loginService;
    private final RefreshSessionService refreshSessionService;
    private final LogoutService logoutService;
    private final LogoutAllService logoutAllService;
    private final RefreshCookieService refreshCookieService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginService.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.buildRefreshCookie(result.refreshToken()))
                .body(new AuthTokenResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(HttpServletRequest request) {
        String refreshToken = refreshCookieService.extractRefreshToken(request)
                .orElseThrow(InvalidRefreshTokenException::new);
        RefreshSessionResult result = refreshSessionService.refresh(new RefreshSessionCommand(refreshToken));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.buildRefreshCookie(result.refreshToken()))
                .body(new AuthTokenResponse(result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        refreshCookieService.extractRefreshToken(request)
                .ifPresent(token -> logoutService.logout(new RefreshSessionCommand(token)));

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.clearRefreshCookie())
                .build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest request) {
        String refreshToken = refreshCookieService.extractRefreshToken(request)
                .orElseThrow(InvalidRefreshTokenException::new);
        logoutAllService.logoutAll(new RefreshSessionCommand(refreshToken));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieService.clearRefreshCookie())
                .build();
    }
}
