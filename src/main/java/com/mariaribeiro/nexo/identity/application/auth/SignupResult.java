package com.mariaribeiro.nexo.identity.application.auth;

import java.time.Instant;

public record SignupResult(String accessToken, Instant expiresAt, String refreshToken) {
}

