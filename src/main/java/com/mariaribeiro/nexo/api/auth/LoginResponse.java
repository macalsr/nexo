package com.mariaribeiro.nexo.api.auth;

import java.time.Instant;

public record LoginResponse(String accessToken, Instant expiresAt) {
}
