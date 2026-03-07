package com.mariaribeiro.nexo.identity.application.usecase;

import java.time.Instant;

public record RefreshSessionResult(String accessToken, Instant expiresAt, String refreshToken) {
}
