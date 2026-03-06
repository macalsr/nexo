package com.mariaribeiro.nexo.identity.application.usecase;

import java.time.Instant;
import java.util.UUID;

public record TokenClaims(UUID userId, String email, Instant expiresAt) {
}
