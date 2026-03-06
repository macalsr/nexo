package com.mariaribeiro.nexo.application.auth;

import java.time.Instant;

public record LoginResult(String accessToken, Instant expiresAt) {
}
