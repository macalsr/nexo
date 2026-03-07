package com.mariaribeiro.nexo.identity.application.auth;

import java.time.Instant;

public record RefreshSessionResult(String accessToken, Instant expiresAt, String refreshToken) {
}

