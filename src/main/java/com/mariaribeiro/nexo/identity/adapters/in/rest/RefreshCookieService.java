package com.mariaribeiro.nexo.identity.adapters.in.rest;

import com.mariaribeiro.nexo.identity.adapters.out.security.RefreshTokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshCookieService {

    private final RefreshTokenProperties refreshTokenProperties;

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
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

    public String buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenProperties.getCookieName(), refreshToken)
                .httpOnly(true)
                .secure(refreshTokenProperties.isCookieSecure())
                .sameSite(refreshTokenProperties.getCookieSameSite())
                .path(refreshTokenProperties.getCookiePath())
                .maxAge(refreshTokenProperties.getTtl())
                .build()
                .toString();
    }

    public String clearRefreshCookie() {
        return ResponseCookie.from(refreshTokenProperties.getCookieName(), "")
                .httpOnly(true)
                .secure(refreshTokenProperties.isCookieSecure())
                .sameSite(refreshTokenProperties.getCookieSameSite())
                .path(refreshTokenProperties.getCookiePath())
                .maxAge(Duration.ZERO)
                .build()
                .toString();
    }
}

