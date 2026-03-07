package com.mariaribeiro.nexo.identity.application.usecase;

import java.time.Instant;

public record LoginResult(String accessToken, Instant expiresAt, String refreshToken) {
}
