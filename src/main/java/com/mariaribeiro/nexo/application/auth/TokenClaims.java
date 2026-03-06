package com.mariaribeiro.nexo.application.auth;

import java.time.Instant;
import java.util.UUID;

public record TokenClaims(UUID userId, String email, Instant expiresAt) {
}
