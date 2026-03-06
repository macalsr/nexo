package com.mariaribeiro.nexo.identity.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PasswordResetToken(
        UUID id,
        UUID userId,
        String token,
        Instant expiresAt,
        Instant createdAt) {

    public PasswordResetToken {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
    }

    public static PasswordResetToken issue(UUID userId, String token, Instant issuedAt, Duration ttl) {
        return new PasswordResetToken(
                UUID.randomUUID(),
                userId,
                token,
                issuedAt.plus(ttl),
                issuedAt);
    }
}
