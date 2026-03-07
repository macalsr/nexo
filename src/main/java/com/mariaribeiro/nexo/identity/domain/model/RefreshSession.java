package com.mariaribeiro.nexo.identity.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class RefreshSession {
    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;

    private Instant lastUsedAt;
    private Instant revokedAt;
    private UUID replacedBySessionId;

    private RefreshSession(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant lastUsedAt,
            Instant revokedAt,
            UUID replacedBySessionId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash must not be blank");
        }

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }

        if (lastUsedAt != null && lastUsedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("lastUsedAt must not be before createdAt");
        }

        if (revokedAt != null && revokedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("revokedAt must not be before createdAt");
        }

        if (replacedBySessionId != null && revokedAt == null) {
            throw new IllegalArgumentException("replacedBySessionId requires revokedAt");
        }

        this.lastUsedAt = lastUsedAt;
        this.revokedAt = revokedAt;
        this.replacedBySessionId = replacedBySessionId;
    }

    public static RefreshSession create(UUID userId, String tokenHash, Instant issuedAt, Duration ttl) {
        Objects.requireNonNull(ttl, "ttl must not be null");
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }

        return new RefreshSession(
                UUID.randomUUID(),
                userId,
                tokenHash,
                issuedAt.plus(ttl),
                issuedAt,
                null,
                null,
                null);
    }

    public static RefreshSession restore(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant lastUsedAt,
            Instant revokedAt,
            UUID replacedBySessionId) {
        return new RefreshSession(
                id,
                userId,
                tokenHash,
                expiresAt,
                createdAt,
                lastUsedAt,
                revokedAt,
                replacedBySessionId);
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant lastUsedAt() {
        return lastUsedAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    public UUID replacedBySessionId() {
        return replacedBySessionId;
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive(Instant now) {
        return !isExpired(now) && !isRevoked();
    }

    public void markUsed(Instant now) {
        requireActive(now);
        this.lastUsedAt = now;
    }

    public void revoke(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (revokedAt == null) {
            this.revokedAt = now;
        }
    }

    public void rotateTo(UUID newSessionId, Instant now) {
        Objects.requireNonNull(newSessionId, "newSessionId must not be null");
        requireActive(now);
        if (replacedBySessionId != null) {
            throw new IllegalStateException("session is already rotated");
        }

        this.replacedBySessionId = newSessionId;
        this.revokedAt = now;
    }

    private void requireActive(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (!isActive(now)) {
            throw new IllegalStateException("session is not active");
        }
    }
}

